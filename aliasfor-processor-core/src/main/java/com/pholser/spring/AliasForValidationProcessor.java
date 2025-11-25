package com.pholser.spring;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner8;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Annotation processor that statically validates Spring's
 * {@code @AliasFor} usage.
 * <p>
 * It checks:
 * <ul>
 *   <li>Structural rules on annotation types that use {@code @AliasFor}</li>
 *   <li>Conflicting alias values on usages (for aliases within the same
 *   annotation)</li>
 * </ul>
 * <p>
 * Note: This processor does NOT depend on Spring at compile time. It only
 * refers to {@code org.springframework.core.annotation.AliasFor} by
 * fully-qualified name.
 */
@SupportedAnnotationTypes(FullyQualifiedClassNames.ALIAS_FOR)
public class AliasForValidationProcessor extends AbstractProcessor {
  private Types typeUtils;
  private Elements elementUtils;
  private Messager messager;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    this.typeUtils = processingEnv.getTypeUtils();
    this.elementUtils = processingEnv.getElementUtils();
    this.messager = processingEnv.getMessager();
  }

  @Override
  public boolean process(
    Set<? extends TypeElement> annotations,
    RoundEnvironment roundEnv) {

    if (annotations.isEmpty()) {
      return false;
    }

    TypeElement aliasForElement =
      elementUtils.getTypeElement(FullyQualifiedClassNames.ALIAS_FOR);
    if (aliasForElement == null) {
      // Spring not on the compile classpath for this module; nothing to do
      return false;
    }

    // Phase 1: collect alias metadata
    Map<String, AnnotationAliasModel> aliasModels =
      collectAliasModels(roundEnv, aliasForElement);
    if (aliasModels.isEmpty()) {
      return false;
    }

    // Phase 2: validate annotation types
    validateAnnotationTypes(aliasModels);

    // Phase 3: validate annotation usages
    validateAnnotationUsages(aliasModels, roundEnv);

    // Returning false lets other processors also see @AliasFor if they want
    return false;
  }

  /**
   * Represents a single {@code @AliasFor} relationship.
   */
  record AliasDescriptor(
    ExecutableElement sourceMethod,
    String sourceAttributeName,
    TypeElement declaringAnno,
    ExecutableElement targetMethod,
    String targetAttributeName,
    TypeElement targetAnno) {
  }

  /**
   * All alias relationships for a particular annotation type.
   */
  static final class AnnotationAliasModel {
    private final TypeElement annoType;
    private final List<AliasDescriptor> aliases;

    AnnotationAliasModel(TypeElement annoType, List<AliasDescriptor> aliases) {
      this.annoType = annoType;
      this.aliases = Collections.unmodifiableList(new ArrayList<>(aliases));
    }

    TypeElement annotationType() {
      return annoType;
    }

    List<AliasDescriptor> aliases() {
      return aliases;
    }
  }

  // ============================================================
  //  Phase 1: collect alias metadata
  // ============================================================

  private Map<String, AnnotationAliasModel> collectAliasModels(
    RoundEnvironment roundEnv,
    TypeElement aliasForElement) {

    Map<String, List<AliasDescriptor>> byAnnotation = new HashMap<>();

    for (Element el : roundEnv.getElementsAnnotatedWith(aliasForElement)) {
      if (el.getKind() != ElementKind.METHOD) {
        messager.printMessage(
          Diagnostic.Kind.ERROR,
          "@AliasFor can only be used on annotation attribute methods",
          el
        );
        continue;
      }

      ExecutableElement method = (ExecutableElement) el;
      Element enclosing = method.getEnclosingElement();
      if (!(enclosing instanceof TypeElement declaringAnno)) {
        messager.printMessage(
          Diagnostic.Kind.ERROR,
          "@AliasFor used on a method not enclosed in an annotation type",
          el
        );
        continue;
      }

      if (declaringAnno.getKind() != ElementKind.ANNOTATION_TYPE) {
        messager.printMessage(
          Diagnostic.Kind.ERROR,
          "@AliasFor used on a method not enclosed in an annotation type",
          el
        );
        continue;
      }

      AnnotationMirror aliasMirror = findAliasForMirror(method, aliasForElement);
      if (aliasMirror == null) {
        // Should not happen if getElementsAnnotatedWith worked,
        // but guard anyway
        continue;
      }

      AliasDescriptor desc =
        buildAliasDescriptor(aliasMirror, method, declaringAnno);
      if (desc == null) {
        // Errors already reported
        continue;
      }

      String key = declaringAnno.getQualifiedName().toString();
      byAnnotation
        .computeIfAbsent(key, k -> new ArrayList<>())
        .add(desc);
    }

    Map<String, AnnotationAliasModel> result = new HashMap<>();
    for (var entry : byAnnotation.entrySet()) {
      List<AliasDescriptor> list = entry.getValue();
      if (list.isEmpty()) {
        continue;
      }
      TypeElement annoType = list.get(0).declaringAnno();
      result.put(entry.getKey(), new AnnotationAliasModel(annoType, list));
    }

    return result;
  }

  private AnnotationMirror findAliasForMirror(
    ExecutableElement method,
    TypeElement aliasForElement) {

    return method.getAnnotationMirrors().stream()
      .filter(m ->
        typeUtils.isSameType(m.getAnnotationType(), aliasForElement.asType()))
      .findFirst()
      .orElse(null);
  }

  private AliasDescriptor buildAliasDescriptor(
    AnnotationMirror aliasMirror,
    ExecutableElement sourceMethod,
    TypeElement declaringAnno) {

    var valuesWithDefaults =
      elementUtils.getElementValuesWithDefaults(aliasMirror);

    // Extract @AliasFor elements: annotation, attribute, value
    TypeElement targetAnnoType =
      resolveTargetAnnotationType(
        valuesWithDefaults,
        declaringAnno,
        aliasMirror,
        sourceMethod);
    if (targetAnnoType == null) {
      return null; // error reported
    }

    String targetAttributeName =
      resolveTargetAttributeName(valuesWithDefaults, aliasMirror, sourceMethod);
    if (targetAttributeName == null) {
      return null; // error reported
    }

    ExecutableElement targetMethod =
      findAttributeMethod(targetAnnoType, targetAttributeName, sourceMethod);
    if (targetMethod == null) {
      return null; // error reported
    }

    return new AliasDescriptor(
      sourceMethod,
      sourceMethod.getSimpleName().toString(),
      declaringAnno,
      targetMethod,
      targetAttributeName,
      targetAnnoType);
  }

  private TypeElement resolveTargetAnnotationType(
    Map<? extends ExecutableElement, ? extends AnnotationValue> aliasValues,
    TypeElement declaringAnno,
    AnnotationMirror aliasMirror,
    Element context) {

    TypeMirror annoTypeMirror = null;

    for (var entry : aliasValues.entrySet()) {
      String name = entry.getKey().getSimpleName().toString();
      if ("annotation".equals(name)) {
        Object v = entry.getValue().getValue();
        if (v instanceof TypeMirror) {
          annoTypeMirror = (TypeMirror) v;
        }
        break;
      }
    }

    if (annoTypeMirror == null) {
      // Should not happen; default is java.lang.annotation.Annotation
      return declaringAnno;
    }

    String typeName = annoTypeMirror.toString();
    if (FullyQualifiedClassNames.JAVA_ANNOTATION.equals(typeName)) {
      // This means "no explicit annotation set" -> default to declaring annotation
      return declaringAnno;
    }

    if (!(annoTypeMirror instanceof DeclaredType declared)) {
      messager.printMessage(
        Diagnostic.Kind.ERROR,
        "Unsupported type for @AliasFor.annotation: " + annoTypeMirror,
        context,
        aliasMirror);
      return null;
    }

    Element el = declared.asElement();
    if (!(el instanceof TypeElement targetAnno)) {
      messager.printMessage(
        Diagnostic.Kind.ERROR,
        "Could not resolve target annotation type for @AliasFor.annotation()",
        context,
        aliasMirror);
      return null;
    }

    // Ensure declaringAnno is meta-annotated with targetAnno
    if (!isMetaAnnotatedWith(declaringAnno, targetAnno)) {
      messager.printMessage(
        Diagnostic.Kind.ERROR,
        "Annotation @" + declaringAnno.getQualifiedName()
          + " must be meta-annotated with @"
          + targetAnno.getQualifiedName()
          + " when using @AliasFor(annotation = ...)",
        context,
        aliasMirror);
      return null;
    }

    return targetAnno;
  }

  private String resolveTargetAttributeName(
    Map<? extends ExecutableElement, ? extends AnnotationValue> aliasValues,
    AnnotationMirror aliasMirror,
    Element context) {

    String attribute = null;
    String value = null;

    for (var entry : aliasValues.entrySet()) {
      String name = entry.getKey().getSimpleName().toString();
      if ("attribute".equals(name)) {
        attribute = (String) entry.getValue().getValue();
      } else if ("value".equals(name)) {
        value = (String) entry.getValue().getValue();
      }
    }

    if (attribute != null && !attribute.isEmpty()
      && value != null && !value.isEmpty()) {
      messager.printMessage(
        Diagnostic.Kind.ERROR,
        "@AliasFor cannot declare both 'attribute' and 'value'",
        context,
        aliasMirror);
      return null;
    }

    if (attribute != null && !attribute.isEmpty()) {
      return attribute;
    }
    if (value != null && !value.isEmpty()) {
      return value;
    }
    // Matches Spring's default: aliasing "value"
    return "value";
  }

  private ExecutableElement findAttributeMethod(
    TypeElement annoType,
    String attributeName,
    Element context) {

    for (Element enclosed : annoType.getEnclosedElements()) {
      if (enclosed.getKind() == ElementKind.METHOD
        && enclosed.getSimpleName().contentEquals(attributeName)) {

        return (ExecutableElement) enclosed;
      }
    }

    messager.printMessage(
      Diagnostic.Kind.ERROR,
      "Target attribute '" + attributeName + "' not found on annotation @"
        + annoType.getQualifiedName(),
      context);
    return null;
  }

  private boolean isMetaAnnotatedWith(
    TypeElement anno,
    TypeElement potentialMeta) {

    for (AnnotationMirror mirror : anno.getAnnotationMirrors()) {
      if (typeUtils.isSameType(mirror.getAnnotationType(), potentialMeta.asType())) {
        return true;
      }
    }

    return false;
  }

  // ============================================================
  //  Phase 2: validate annotation types (structural rules)
  // ============================================================

  private void validateAnnotationTypes(
    Map<String, AnnotationAliasModel> aliasModels) {

    for (AnnotationAliasModel model : aliasModels.values()) {
      for (AliasDescriptor alias : model.aliases()) {
        validateAliasPair(alias);
      }
      // Future work: alias graph analysis (cycles, transitivity).
    }
  }

  private void validateAliasPair(AliasDescriptor alias) {
    ExecutableElement source = alias.sourceMethod();
    ExecutableElement target = alias.targetMethod();

    // 1. Type compatibility
    TypeMirror sourceType = source.getReturnType();
    TypeMirror targetType = target.getReturnType();
    if (!typeUtils.isSameType(sourceType, targetType)) {
      messager.printMessage(
        Diagnostic.Kind.ERROR,
        "@AliasFor attributes must have the same return type; found "
          + sourceType + " and " + targetType,
        source);
    }

    // 2. Default compatibility
    AnnotationValue sourceDefault = source.getDefaultValue();
    AnnotationValue targetDefault = target.getDefaultValue();

    if (sourceDefault == null || targetDefault == null) {
      // You can relax this if you want. Spring generally expects defaults.
      messager.printMessage(
        Diagnostic.Kind.ERROR,
        "@AliasFor attributes must declare default values",
        source);
      return;
    }

    if (!annotationValueEquals(sourceDefault, targetDefault)) {
      messager.printMessage(
        Diagnostic.Kind.ERROR,
        "@AliasFor attributes must declare the same default value; found "
          + formatAnnotationValue(sourceDefault) + " vs "
          + formatAnnotationValue(targetDefault),
        source);
    }
  }

  private boolean annotationValueEquals(AnnotationValue a, AnnotationValue b) {
    Object av = a.getValue();
    Object bv = b.getValue();

    if (av instanceof TypeMirror && bv instanceof TypeMirror) {
      return typeUtils.isSameType((TypeMirror) av, (TypeMirror) bv);
    }

    if (av instanceof List && bv instanceof List) {
      @SuppressWarnings("unchecked")
      List<AnnotationValue> la = (List<AnnotationValue>) av;
      @SuppressWarnings("unchecked")
      List<AnnotationValue> lb = (List<AnnotationValue>) bv;
      if (la.size() != lb.size()) {
        return false;
      }
      for (int i = 0; i < la.size(); i++) {
        if (!annotationValueEquals(la.get(i), lb.get(i))) {
          return false;
        }
      }
      return true;
    }

    return Objects.equals(av, bv);
  }

  private String formatAnnotationValue(AnnotationValue v) {
    Object value = v.getValue();
    if (value instanceof List) {
      @SuppressWarnings("unchecked")
      List<AnnotationValue> list = (List<AnnotationValue>) value;
      return list.stream()
        .map(this::formatAnnotationValue)
        .collect(Collectors.joining(", ", "[", "]"));
    }
    return String.valueOf(value);
  }

  // ============================================================
  //  Phase 3: validate annotation usages (conflicting alias values)
  // ============================================================

  private void validateAnnotationUsages(
    Map<String, AnnotationAliasModel> aliasModels,
    RoundEnvironment roundEnv) {

    if (aliasModels.isEmpty()) {
      return;
    }

    // Map by fully-qualified name for quick lookup
    Map<Name, AnnotationAliasModel> byTypeName =
      aliasModels.values().stream()
        .collect(Collectors.toMap(
          m -> m.annotationType().getQualifiedName(),
          m -> m));

    ElementScanner8<Void, Void> scanner = new ElementScanner8<>() {
      @Override
      public Void scan(Element e, Void p) {
        validateElementAnnotations(e, byTypeName);
        return super.scan(e, p);
      }
    };

    for (Element root : roundEnv.getRootElements()) {
      scanner.scan(root);
    }
  }

  private void validateElementAnnotations(
    Element element,
    Map<Name, AnnotationAliasModel> aliasModels) {

    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      TypeElement annoType = (TypeElement) mirror.getAnnotationType().asElement();
      AnnotationAliasModel model = aliasModels.get(annoType.getQualifiedName());
      if (model != null) {
        validateSingleAnnotationUsage(mirror, model, element);
      }
    }
  }

  private void validateSingleAnnotationUsage(
    AnnotationMirror mirror,
    AnnotationAliasModel model,
    Element context) {

    // Explicit values only. Defaults were already validated structurally.
    var explicitValues = mirror.getElementValues();
    if (explicitValues.isEmpty()) {
      return;
    }

    Map<String, AnnotationValue> byName = new HashMap<>();
    for (var entry : explicitValues.entrySet()) {
      byName.put(entry.getKey().getSimpleName().toString(), entry.getValue());
    }

    for (AliasDescriptor alias : model.aliases()) {
      // For now, only check aliases within the same annotation type.
      if (!typeUtils.isSameType(
        alias.declaringAnno().asType(),
        alias.targetAnno().asType())) {
        continue;
      }

      AnnotationValue left = byName.get(alias.sourceAttributeName());
      AnnotationValue right = byName.get(alias.targetAttributeName());

      // If one or both are not explicitly set, nothing to check.
      if (left == null || right == null) {
        continue;
      }

      if (!annotationValueEquals(left, right)) {
        messager.printMessage(
          Diagnostic.Kind.ERROR,
          "Attributes '" + alias.sourceAttributeName() + "' and '"
            + alias.targetAttributeName()
            + "' are aliases and must have the same value; found "
            + formatAnnotationValue(left) + " vs "
            + formatAnnotationValue(right),
          context,
          mirror);
      }
    }
  }
}
