package eme.extractor;

import static eme.extractor.JDTUtil.getModifier;
import static eme.extractor.JDTUtil.getName;
import static eme.extractor.JDTUtil.isAbstract;
import static eme.extractor.JDTUtil.isEnum;
import static eme.extractor.JDTUtil.isFinal;
import static eme.extractor.JDTUtil.isStatic;
import static eme.extractor.JDTUtil.isVoid;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;

import eme.model.ExtractedMethod;
import eme.model.ExtractedType;
import eme.model.MethodType;
import eme.model.datatypes.ExtractedField;

/**
 * Extractor class for Java Members (Methods and fields). Uses the class {@link DataTypeExtractor}.
 * @author Timur Saglam
 */
public class JavaMemberExtractor {
    private final DataTypeExtractor dataTypeExtractor;

    /**
     * Basic constructor.
     * @param dataTypeExtractor sets the {@link DataTypeExtractor}.
     */
    public JavaMemberExtractor(DataTypeExtractor dataTypeExtractor) {
        this.dataTypeExtractor = dataTypeExtractor;
    }

    /**
     * Parses Fields from an {@link IType} and adds them to an {@link ExtractedType}.
     * @param type is the {@link IType}.
     * @param extractedType is the {@link ExtractedType}.
     * @throws JavaModelException if there are problem with the JDT API.
     */
    public void extractFields(IType type, ExtractedType extractedType) throws JavaModelException {
        ExtractedField extractedField;
        for (IField field : type.getFields()) {
            if (!isEnum(field)) { // if is no enumeral
                extractedField = dataTypeExtractor.extractField(field, type);
                extractedField.setFinal(isFinal(field));
                extractedField.setStatic(isStatic(field));
                extractedField.setModifier(getModifier(field));
                extractedType.addField(extractedField);
            }
        }
    }

    /**
     * Parses the {@link IMethod}s from an {@link IType} and adds them to an ExtractedType.
     * @param type is the {@link IType} whose methods get extracted.
     * @param extractedType is the extracted type where the extracted methods should be added.
     * @throws JavaModelException if there are problem with the JDT API.
     */
    public void extractMethods(IType type, ExtractedType extractedType) throws JavaModelException {
        ExtractedMethod extractedMethod;
        String methodName; // name of the extracted method
        for (IMethod method : type.getMethods()) { // for every method
            methodName = getName(type) + "." + method.getElementName(); // build name
            extractedMethod = new ExtractedMethod(methodName, dataTypeExtractor.extractReturnType(method));
            extractModifiers(method, extractedMethod);
            ITypeParameter[] typeParameters = method.getTypeParameters();
            extractedMethod.setTypeParameters(dataTypeExtractor.extractTypeParameters(typeParameters, type));
            for (ILocalVariable parameter : method.getParameters()) { // extract parameters:
                extractedMethod.addParameter(dataTypeExtractor.extractParameter(parameter, method));
            }
            for (String exception : method.getExceptionTypes()) { // extract throw declarations:
                extractedMethod.addThrowsDeclaration(dataTypeExtractor.extractDataType(exception, type));
            }
            extractedType.addMethod(extractedMethod);
        }
    }

    /**
     * Parses the {@link MethodType} of an {@link IMethod}.
     */
    private MethodType extractMethodType(IMethod method) throws JavaModelException {
        if (method.isConstructor()) {
            return MethodType.CONSTRUCTOR;
        } else if (isAccessor(method)) {
            return MethodType.ACCESSOR;
        } else if (isMutator(method)) {
            return MethodType.MUTATOR;
        } else if (method.isMainMethod()) {
            return MethodType.MAIN;
        }
        return MethodType.NORMAL;
    }

    /**
     * Extracts modifiers from an {@link IMethod} and adds them to an {@link ExtractedMethod}.
     */
    private void extractModifiers(IMethod method, ExtractedMethod extractedMethod) throws JavaModelException {
        extractedMethod.setAbstract(isAbstract(method));
        extractedMethod.setStatic(isStatic(method));
        extractedMethod.setMethodType(extractMethodType(method));
        extractedMethod.setModifier(getModifier(method));
    }

    /**
     * Retrieves the correlating field of a method from its access method naming scheme (depending on the prefixes).
     */
    private IField findReferencedField(IMethod method, String... prefixes) throws JavaModelException {
        IType type = method.getDeclaringType();
        for (IField field : type.getFields()) { // for ever field of IType:
            for (String prefix : prefixes) { // for every given prefix
                if (method.getElementName().equalsIgnoreCase(prefix + field.getElementName())) { // fits method name?
                    return field; // is access method if name scheme fits for one field
                }
            }
        }
        return null; // is not an access method if no field fits
    }

    /**
     * Checks whether a {@link IMethod} is an access method (either an accessor or an mutator, depending on the prefixes).
     */
    private boolean isAccessMethod(IMethod method, String... prefixes) throws JavaModelException {
        return findReferencedField(method, prefixes) != null;
    }

    /**
     * Checks whether a {@link IMethod} is an accessor method.
     */
    private boolean isAccessor(IMethod method) throws JavaModelException {
        if (isAccessMethod(method, "get", "is") && method.getNumberOfParameters() == 0 && !isVoid(method.getReturnType())) { // if name fits
            String field = findReferencedField(method, "get", "is").getTypeSignature();
            System.err.println(method.getElementName() + " cmp: " + method.getReturnType() + " <> " + field);
            return method.getReturnType().equals(field);
        }
        return false;
    }

    /**
     * Checks whether a {@link IMethod} is a mutator method.
     */
    private boolean isMutator(IMethod method) throws JavaModelException {
        if (isAccessMethod(method, "set") && method.getNumberOfParameters() == 1 && isVoid(method.getReturnType())) { // if name fits
            String field = findReferencedField(method, "set").getTypeSignature();
            System.err.println(method.getElementName() + " cmp: " + method.getParameters()[0].getTypeSignature() + " <> " + field);
            return method.getParameters()[0].getTypeSignature().equals(field);
        }
        return false;
    }
}