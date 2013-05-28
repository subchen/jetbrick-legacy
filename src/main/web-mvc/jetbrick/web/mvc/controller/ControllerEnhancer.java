package jetbrick.web.mvc.controller;

import java.io.InvalidClassException;
import java.security.ProtectionDomain;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import jetbrick.web.mvc.RequestContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;

/**
 * 用javassist字节码增强技术，将Controller类进行动态扩展，以后调用，就可以不用反射了。
 */
public class ControllerEnhancer {
	private static ClassPool pool = ClassPool.getDefault();
	private static final String actionContextClass = RequestContext.class.getName();

	@SuppressWarnings("unchecked")
	public static Class<Controller> loadClass(String className, ClassLoader classLoader, ProtectionDomain domain) throws Exception {
		CtClass cc = create(className);
		return cc.toClass(classLoader, domain);
	}

	public static CtClass create(String className) throws Exception {
		CtClass cc = pool.get(className);

		CtClass controller = pool.get(Controller.class.getName());
		cc.addInterface(controller);

		CtMethod[] methods = cc.getMethods();

		StringBuilder src = new StringBuilder();
		src.append("public com.humpic.framework.web.mvc.Result __main__(com.humpic.framework.web.mvc.ActionContext $ac) throws Throwable {");
		src.append("  com.humpic.framework.web.mvc.Result $result = null;");
		src.append("  boolean $found = false;");
		src.append(buildBeforeMethod(methods));

		src.append("  String $actionName = $ac.getActionName();");
		for (CtMethod cm : methods) {
			if (cm.hasAnnotation(Action.class)) {
				src.append(buildActionSource(cm));
			}
		}
		src.append("  if (!$found) throw new RuntimeException(\"action not found:\" + $actionName);");
		src.append(buildAfterMethod(methods));
		src.append("  return $result;");
		src.append("}");

		CtMethod actionEntry = CtNewMethod.make(src.toString(), cc);
		cc.addMethod(actionEntry);

		//cc.writeFile("/tmp/");
		return cc;
	}

	private static Object buildBeforeMethod(CtMethod[] methods) throws Exception {
		for (CtMethod cm : methods) {
			if (cm.hasAnnotation(Before.class)) {
				CtClass[] parameterTypes = cm.getParameterTypes();
				if (parameterTypes.length == 1 && actionContextClass.equals(parameterTypes[0].getName())) {
					return cm.getName() + "($ac);";
				} else {
					throw new InvalidClassException(cm.getDeclaringClass().getName(), "Method " + cm.getName() + " cannot be identified as Before.");
				}
			}
		}
		return "";
	}

	private static Object buildAfterMethod(CtMethod[] methods) throws Exception {
		for (CtMethod cm : methods) {
			if (cm.hasAnnotation(After.class)) {
				CtClass[] parameterTypes = cm.getParameterTypes();
				if (parameterTypes.length == 1 && actionContextClass.equals(parameterTypes[0].getName())) {
					return cm.getName() + "($ac);";
				} else {
					throw new InvalidClassException(cm.getDeclaringClass().getName(), "Method " + cm.getName() + " cannot be identified as After.");
				}
			}
		}
		return "";
	}

	private static String buildActionSource(CtMethod cm) throws Exception {
		CodeAttribute codeAttribute = cm.getMethodInfo().getCodeAttribute();
		LocalVariableAttribute attr = null;
		if (codeAttribute != null) {
			attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
		}

		StrBuilder sb = new StrBuilder();
		sb.append("if (\"{actionName}\".equals($actionName)) {");
		sb.append("$found = true;");

		CtClass[] parameterTypes = cm.getParameterTypes();
		String[] args = new String[parameterTypes.length];
		if (attr == null) {
			if (parameterTypes.length == 1 && actionContextClass.equals(parameterTypes[0].getName())) {
				args[0] = "$ac";
			} else {
				throw new InvalidClassException(cm.getDeclaringClass().getName(), "Method " + cm.getName() + " cannot be identified as Action.");
			}
		} else {
			int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
			for (int i = 0; i < parameterTypes.length; i++) {
				args[i] = attr.variableName(i + pos);
				String source = buildActionParameterSource(args[i], parameterTypes[i]);
				sb.append(source);
			}
		}
		sb.append("{resultClass} $obj_result = {actionName}( {parameterList} );");
		sb.append("$result = com.humpic.framework.web.mvc.Result.of($obj_result);");
		sb.append("}");

		sb.replaceAll("{actionName}", cm.getName());
		sb.replaceAll("{resultClass}", cm.getReturnType().getName());
		sb.replaceAll("{parameterList}", StringUtils.join(args, ","));

		return sb.toString();
	}

	protected static String buildActionParameterSource(String paramName, CtClass paramClass) throws NotFoundException {
		StrBuilder sb = new StrBuilder();
		String paramClassName = paramClass.getName();
		if (actionContextClass.equals(paramClassName)) {
			sb.append("{paramClass} {name} = $ac;");
		} else if ("form".equals(paramName) && !paramClassName.startsWith("java")) {
			sb.append("{paramClass} {name} = ({paramClass}) $ac.getInputForm(new {paramClass}());");
		} else if ("java.lang.String".equals(paramClassName)) {
			sb.append("String {name} = $ac.getParameter(\"{name}\");");
		} else if (paramClass.isArray()) {
			paramClassName = paramClass.getComponentType().getName();
			sb.append("String[] $param_{name} = $ac.getParameterValues(\"{name}\");");
			sb.append("{paramClass}[] {name} = ({paramClass}[]) jetbrick.commons.bean.ClassConvertUtils.convertArrays($param_{name}, {paramClass}.class);");
		} else if (paramClass.isPrimitive()) {
			paramClassName = ((CtPrimitiveType) paramClass).getWrapperName();
			sb.append("String $param_{name} = $ac.getParameter(\"{name}\");");
			sb.append("{paramClass} $wrap_{name} = ({paramClass}) jetbrick.commons.bean.ClassConvertUtils.convert($param_{name}, {paramClass}.class);");
			sb.append(paramClass.getName() + " {name} = $wrap_{name}." + paramClass.getName() + "Value();");
		} else {
			sb.append("String $param_{name} = $ac.getParameter(\"{name}\");");
			sb.append("{paramClass} {name} = ({paramClass}) jetbrick.commons.bean.ClassConvertUtils.convert($param_{name}, {paramClass}.class);");
		}

		sb.replaceAll("{name}", paramName);
		sb.replaceAll("{paramClass}", paramClassName);

		return sb.toString();
	}

	@SuppressWarnings("unchecked")
    public static void main(String[] args) throws Throwable {
		CtClass cc = ControllerEnhancer.create("test.TestController");
        Class<Controller> clazz = cc.toClass();
		Controller controller = clazz.newInstance();
		controller.__main__(null);
	}
}
