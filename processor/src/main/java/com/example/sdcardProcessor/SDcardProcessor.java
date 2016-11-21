package com.example.sdcardProcessor;

import com.hu.annotation.SDCardRootFile;
import com.example.utils.ProcessorUtil;
import com.example.utils.StringUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

/**
 *  处理器
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.hu.annotation.SDCardRootFile"})
public class SDcardProcessor extends AbstractProcessor {

    private static final String CLASS_NAME = "SDCardUtil";

    private static final String ANNOTATION = "@" + SDCardRootFile.class.getSimpleName();

    //******Field**************************************************************
    private static final String SDCARD_ROOT_FILE_NAME = "mRootFileName";

    //******Method**************************************************************
    private static final String METHOD_INIT_FILE = "initFile";
    private static final String METHOD_GET_SDCARD = "getSDCard%sPath";
    private static final String METHOD_GET_SDCARD_PATH = "getSDCardPath";
    private static final String METHOD_IS_SDCARD_EXIST_REAL = "isSDCardExistReal";
    private static final String METHOD_CREATE_CACHE_FILE = "createCacheFile";
    private static final String METHOD_DELETE = "delete";
    private static final String METHOD_CLEARFILE = "clearFile";

    //******Class**************************************************************
    ClassName mEnvironmentClassName = ClassName.get("android.os", "Environment");
    ClassName mFileClassName = ClassName.get("java.io", "File");

    private Messager messager;
    /**
     * 这里必须指定，这个注解处理器是注册给哪个注解的。注意，它的返回值是一个字符串的集合，
     * 包含本处理器想要处理的注解类型的合法全称。换句话说，在这里定义你的注解处理器注册到哪些注解上。
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(SDCardRootFile.class.getCanonicalName());
        return annotataions;
    }
    /**
     * 用来指定你使用的Java版本。通常这里返回SourceVersion.latestSupported()。
     * 然而，如果有足够的理由只支持Java6的话，也可以返回SourceVersion.RELEASE_6。推荐使用前者。
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
//        return SourceVersion.latestSupported();
    }

    /**
     * 每一个注解处理器类都必须有一个空的构造函数。然而，这里有一个特殊的init()方法，
     * 它会被注解处理工具调用，并输入ProcessingEnviroment参数。
     * ProcessingEnviroment提供很多有用的工具类Elements,Types和Filer。
     * @param processingEnv
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    /**
     * 这相当于每个处理器的主函数main()。在这里写扫描、评估和处理注解的代码，
     * 以及生成Java文件。输入参数RoundEnviroment，可以让查询出包含特定注解的被注解元素。
     * @param annotations
     * @param roundEnv
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ArrayList<SDcardAnnotatedClass> annotatedClasses = new ArrayList<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(SDCardRootFile.class)) {
            //if public Field 遍历所有的file是否和规则
            if (!ProcessorUtil.isFinalValidField(element, messager, ANNOTATION)) {
                return true;
            }
            //符合规则以后，获取方法的参数
            VariableElement variableElement = (VariableElement) element;
            //打印
            messager.printMessage(Diagnostic.Kind.NOTE,
                    "Annotation class : className = " + element.getSimpleName().toString());
            try {
                //paser
                annotatedClasses.add(buildAnnotVariabldSDcardClass(variableElement));
            } catch (IOException e) {
                String message = String.format("Couldn't processvariablass %s: .%s", variableElement,
                        e.getMessage());
                messager.printMessage(Diagnostic.Kind.ERROR, message, element);
            }
        }
        try {
            //生成代码
            generate(annotatedClasses);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Couldn't generate class");
        }
        return true;
    }

    /**
     * package com.example;    // PackageElement
     * <p/>
     * public class Foo {        // TypeElement
     * <p/>
     * private int a;      // VariableElement
     * private Foo other;  // VariableElement
     * <p/>
     * public Foo () {}    // ExecuteableElement
     * <p/>
     * public void setA (  // ExecuteableElement
     * int newA   // TypeElement
     * ) {}
     * }
     *
     * @param annotatedClass
     * @return
     * @throws IOException
     */
    private SDcardAnnotatedClass buildAnnotVariabldSDcardClass(VariableElement annotatedClass)
            throws IOException {
        return new SDcardAnnotatedClass(annotatedClass);
    }


    private void generate(List<SDcardAnnotatedClass> classList) throws IOException {
        if (null == classList || classList.size() == 0) {
            return;
        }
        for (int i = 0; i < classList.size(); i++) {
            //getElementUtils()返回用来在元素上进行操作的某些实用工具方法的实现。
            String packageName = ProcessorUtil.getPackageName(processingEnv.getElementUtils(), classList.get(i).getQualifiedSuperClassName());
            TypeSpec generateClass = generateClass(classList.get(i));

            JavaFile javaFile = JavaFile.builder(packageName, generateClass).
                    build();
            //getFiler() 返回用来创建新源、类或辅助文件的 Filer。
            javaFile.writeTo(processingEnv.getFiler());
        }
    }

    public TypeSpec generateClass(SDcardAnnotatedClass classes) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(classes.getAppRootPathName() + CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        builder.addField(makeCreateField(SDCARD_ROOT_FILE_NAME, classes.getAppRootPathName()));

        List<String> initfileMethodNames = new ArrayList<>();

        for (String fileName : classes.getFileNames()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "fileName=" + fileName);
            builder.addField(makeCreateField(fileName, fileName));

            MethodSpec methodSpec = makeFilePathMethod(fileName);
            builder.addMethod(methodSpec);
            initfileMethodNames.add(methodSpec.name);
        }

        builder.addStaticBlock(makeStaticb());
        builder.addMethod(makeCreateCacheFile(initfileMethodNames));
        builder.addMethod(makeIsDdcardExistRealMethod());
        builder.addMethod(makeGetSDCardPthMethod());
        builder.addMethod(maekInitFileMethod());
        builder.addMethod(makeDeleFileMethod());
        builder.addMethod(makeClearFileMethod());

        return builder.build();
    }

    /**
     * .initializer("$S + $L", "Lollipop v.", 5.0d)
     *  创建field
     * @param fieldName
     * @param value
     * @return
     */
    private FieldSpec makeCreateField(String fieldName, String value) {
        return FieldSpec.builder(String.class, fieldName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", value)
                .build();

    }

    /**
     * 静态代码块
     * @return
     */
    private CodeBlock makeStaticb() {
        return CodeBlock.builder()
                .addStatement(METHOD_CREATE_CACHE_FILE + "()")
                .build();
    }

    /**
     * 删除文件的方法
     * @return
     */
    private MethodSpec makeClearFileMethod() {
        return MethodSpec.methodBuilder(METHOD_CLEARFILE)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("String path=" + METHOD_GET_SDCARD_PATH + "()")
                .addStatement(METHOD_DELETE + "(new $T(path))", mFileClassName)
                .addStatement(METHOD_CREATE_CACHE_FILE + "()")
                .build();
    }

    /**
     * 删除file的方法
     * @return
     */
    private MethodSpec makeDeleFileMethod() {
        return MethodSpec.methodBuilder(METHOD_DELETE)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(mFileClassName, "file")
                .beginControlFlow("if(file.isFile())")
                .addStatement("file.delete();")
                .addStatement("return")
                .endControlFlow()
                .beginControlFlow("if(file.isDirectory())")
                .addStatement("File[] childFiles = file.listFiles()")
                .beginControlFlow("if (childFiles == null || childFiles.length == 0)")
                .addStatement("file.delete()")
                .addStatement("return")
                .endControlFlow()
                .beginControlFlow("for (int i = 0; i < childFiles.length; i++)")
                .addStatement(METHOD_DELETE + "(childFiles[i])")
                .endControlFlow()
                .addStatement("file.delete()")
                .endControlFlow()
                .build();
    }

    /**
     * 创建文件的方法
     * @param methodList
     * @return
     */
    private MethodSpec makeCreateCacheFile(List<String> methodList) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(METHOD_CREATE_CACHE_FILE)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        for (String methodName : methodList) {
            builder.addStatement(METHOD_INIT_FILE + "(" + methodName + "())");
        }
        return builder.build();
    }

    /**
     * 获取sd的path的方法
     * @param fileName
     * @return
     */
    private MethodSpec makeFilePathMethod(String fileName) {
        return MethodSpec.methodBuilder(String.format(METHOD_GET_SDCARD, StringUtils.capitalize(fileName)))
                .returns(String.class)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("String path = getSDCardPath() + File.separator + " + fileName)
                .addStatement("return path")
                .build();
    }

    /**
     * sd卡是否存在
     * @return
     */
    private MethodSpec makeIsDdcardExistRealMethod() {
        return MethodSpec.methodBuilder(METHOD_IS_SDCARD_EXIST_REAL)
                .returns(boolean.class)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("boolean isExits = false")
                .addStatement("isExits = $T.getExternalStorageState().equals($T.MEDIA_MOUNTED)", mEnvironmentClassName, mEnvironmentClassName)
                .addStatement("return isExits")
                .build();
    }

    private MethodSpec makeGetSDCardPthMethod() {
        return MethodSpec.methodBuilder(METHOD_GET_SDCARD_PATH)
                .returns(String.class)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("String path = null")
                .beginControlFlow("if(isSDCardExistReal())")
                .addStatement("path = $T.getExternalStorageDirectory().toString() + $T.separator +"
                        + SDCARD_ROOT_FILE_NAME, mEnvironmentClassName, mFileClassName)
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("path = $T.getDataDirectory().toString() + $T.separator +"
                        + SDCARD_ROOT_FILE_NAME, mEnvironmentClassName, mFileClassName)
                .endControlFlow()
                .addStatement("return path")
                .build();
    }

    private MethodSpec maekInitFileMethod() {
        return MethodSpec.methodBuilder(METHOD_INIT_FILE)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String.class, "path")
                .addStatement("$T file = new $T(path)", mFileClassName, mFileClassName)
                .beginControlFlow("if(file != null && !file.exists())")
                .addStatement("file.mkdirs();")
                .endControlFlow()
                .build();
    }
}
