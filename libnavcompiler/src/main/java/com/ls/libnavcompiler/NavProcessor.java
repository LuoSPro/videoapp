package com.ls.libnavcompiler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ls.libnavannotation.ActivityDestination;
import com.ls.libnavannotation.FragmentDestination;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import com.google.auto.service.AutoService;

/**
 * 编译成功：
 * 1.不要在这个module里面自己调用自己：比如implementation project(':libnavannotation')
 * 2. gradle的版本降下来：4.10.1   和插件 3.2.0
 * 3. AuthService：用这个版本的依赖：annotationProcessor 'com.google.auto.service:auto-service:1.0-rc7'
 *    不然1.0-rc4会报错
 * 4. 注释报错问题：添加
 *           //中文乱码问题（错误：编码GBK不可映射字符）
 *          tasks.withType(JavaCompile) {
 *              options.encoding = "UTF-8"
 *          }
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)//java1.8
@SupportedAnnotationTypes({"com.ls.libnavannotation.FragmentDestination","com.ls.libnavannotation.ActivityDestination"})
public class NavProcessor extends AbstractProcessor {

    private Messager messager;
    private Filer filer;
    private static final String OUTPUT_FILE_NAME = "destination.json";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        //日志打印,在java环境下不能使用android.util.log.e()
        messager = processingEnv.getMessager();
        //文件处理工具
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //通过处理器环境上下文roundEnv分别获取 项目中标记的FragmentDestination.class 和ActivityDestination.class注解。
        //此目的就是为了收集项目中哪些类 被注解标记了
        Set<? extends Element> fragmentElements = roundEnv.getElementsAnnotatedWith(FragmentDestination.class);
        Set<? extends Element> activityElements = roundEnv.getElementsAnnotatedWith(ActivityDestination.class);
        if (!fragmentElements.isEmpty()||!activityElements.isEmpty()){
            HashMap<String, JSONObject> destMap = new HashMap<>();
            //分别 处理FragmentDestination  和 ActivityDestination 注解类型
            //并收集到destMap 这个map中。以此就能记录下所有的页面信息了
            handleDestination(fragmentElements,FragmentDestination.class,destMap);
            handleDestination(activityElements,ActivityDestination.class,destMap);

            FileOutputStream fos = null;
            OutputStreamWriter writer = null;
            try {
                /*生成文件到  app/src/main/resource*/
                FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_FILE_NAME);
                String resourcePath = resource.toUri().getPath();
                messager.printMessage(Diagnostic.Kind.NOTE,"resourcePath --> " + resourcePath );

                //直接进到  /app  目录下
                //由于我们想要把json文件生成在app/src/main/assets/目录下,所以这里可以对字符串做一个截取，
                //以此便能准确获取项目在每个电脑上的 /app/src/main/assets/的路径
                //由于我的包名里面有app，所以需要加8，找到后面一个app所在的位置
                String appPath = resourcePath.substring(0, resourcePath.indexOf("app") + 8);
                messager.printMessage(Diagnostic.Kind.NOTE,"appPath --> " + appPath);
                String assetsPath = appPath + "src/main/assets/";
                messager.printMessage(Diagnostic.Kind.NOTE,"assetsPath --> " + assetsPath);

                File file = new File(assetsPath);
                if (!file.exists()){
                    file.mkdirs();
                }

                //输出文件
                File outPutFile = new File(file,OUTPUT_FILE_NAME);
                if (outPutFile.exists()){
                    outPutFile.delete();
                }
                outPutFile.createNewFile();
                //map转字符串
                String content = JSON.toJSONString(destMap);
                fos = new FileOutputStream(outPutFile);
                writer = new OutputStreamWriter(fos, "UTF-8");
                writer.write(content);
                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (writer != null){
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null){
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }

    private void handleDestination(Set<? extends Element> elements, Class<? extends Annotation> annotationClaz, HashMap<String, JSONObject> destMap) {
        for (Element element : elements) {
            TypeElement typeElement = (TypeElement) element;
            String pageUrl = null;
            String className = typeElement.getQualifiedName().toString();
            //id用hash，但是id不为负数
            int id = Math.abs(className.hashCode());
            //是否需要登录
            boolean needLogin = false;
            //是不是起始页面
            boolean asStarter = false;
            //区分Fragment，还是Activity
            boolean isFragment;

            Annotation annotation = typeElement.getAnnotation(annotationClaz);
            if (annotation instanceof FragmentDestination){
                FragmentDestination dest = (FragmentDestination) annotation;
                pageUrl = dest.pagerUrl();
                asStarter = dest.asStarter();
                needLogin = dest.needLogin();
                isFragment = true;
            }else {
                ActivityDestination dest = (ActivityDestination) annotation;
                pageUrl = dest.pagerUrl();
                asStarter = dest.asStarter();
                needLogin = dest.needLogin();
                isFragment = false;
            }

            if (destMap.containsKey(pageUrl)){
                messager.printMessage(Diagnostic.Kind.ERROR,"不同的页面不允许使用相同的pagerUrl:");
            }else{
                JSONObject object = new JSONObject();
                object.put("id",id);
                object.put("asStarter",asStarter);
                object.put("needLogin",needLogin);
                object.put("pageUrl",pageUrl);
                object.put("isFragment",isFragment);
                object.put("clazName",className);
                //把object放到map中，这样才能把object变成json
                destMap.put(pageUrl,object);
            }
        }
    }
}
