package com.rover12421.shaka.apktool.AspjectJ.apktool_lib;

import brut.androlib.AndrolibException;
import brut.androlib.res.data.ResResSpec;
import brut.androlib.res.data.ResResource;
import brut.androlib.res.data.value.ResAttr;
import brut.androlib.res.data.value.ResReferenceValue;
import brut.androlib.res.data.value.ResScalarValue;
import brut.androlib.res.data.value.ResValue;
import brut.util.Duo;
import com.rover12421.shaka.apktool.util.ReflectUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by rover12421 on 8/9/14.
 */
@Aspect
public class ResStyleValueAj {
    /**
     * package brut.androlib.res.data.value
     * public class ResStyleValue extends ResBagValue implements ResValuesXmlSerializable
     * public void serializeToResValuesXml(XmlSerializer serializer,ResResource res)
     *

     <B>com.carrot.carrotfantasy.apk</B>

     Exception in thread "main" java.lang.ClassCastException: brut.androlib.res.data.value.ResStyleValue cannot be cast to brut.androlib.res.data.value.ResAttr
     at brut.androlib.res.data.value.ResStyleValue.serializeToResValuesXml(ResStyleValue.java:65)
     at brut.androlib.res.AndrolibResources.generateValuesFile(AndrolibResources.java:564)
     at brut.androlib.res.AndrolibResources.decode(AndrolibResources.java:327)
     at brut.androlib.Androlib.decodeResourcesFull(Androlib.java:123)
     at brut.androlib.ApkDecoder.decode(ApkDecoder.java:102)
     at brut.apktool.Main.cmdDecode(Main.java:170)
     at brut.apktool.Main.main(Main.java:86)

     */
    @Around("execution(void brut.androlib.res.data.value.ResStyleValue.serializeToResValuesXml(..))" +
            "&& args(serializer, res)")
    public void serializeToResValuesXml(ProceedingJoinPoint joinPoint, XmlSerializer serializer,
                                        ResResource res) throws IOException, AndrolibException {
        try {
            ResReferenceValue mParent = (ResReferenceValue) ReflectUtil.getFieldValue(joinPoint.getThis(), "mParent");
            Duo<ResReferenceValue, ResScalarValue>[] mItems = (Duo<ResReferenceValue, ResScalarValue>[]) ReflectUtil.getFieldValue(joinPoint.getThis(), "mItems");
            serializer.startTag(null, "style");
            serializer.attribute(null, "name", res.getResSpec().getName());
            if (!mParent.isNull()) {
                serializer.attribute(null, "parent", mParent.encodeAsResXmlAttr());
            } else if (res.getResSpec().getName().indexOf('.') != -1) {
                serializer.attribute(null, "parent", "");
            }
            for (int i = 0; i < mItems.length; i++) {
                ResResSpec spec = mItems[i].m1.getReferent();
                String name = null;
                String value = null;

                String resource = spec.getDefaultResource().getValue().toString();
                // hacky-fix remove bad ReferenceVars
                if (resource.contains("ResReferenceValue@")) {
                    continue;
                } else if (resource.contains("ResStringValue@")) {
                    name = "@" + spec.getFullName(res.getResSpec().getPackage(), false);
                } else {
//                    ResAttr attr = (ResAttr) spec.getDefaultResource().getValue();
                    ResValue resValue = spec.getDefaultResource().getValue();
                    if (!(resValue instanceof ResAttr)) {
                        //异常跳过
                        continue;
                    }
                    ResAttr attr = (ResAttr)resValue;
                    value = attr.convertToResXmlFormat(mItems[i].m2);
                    name = spec.getFullName(res.getResSpec().getPackage(), true);
                }

                if (value == null) {
                    value = mItems[i].m2.encodeAsResXmlValue();
                }

                if (value == null) {
                    continue;
                }

                serializer.startTag(null, "item");
                serializer.attribute(null, "name", name);
                serializer.text(value);
                serializer.endTag(null, "item");
            }
            serializer.endTag(null, "style");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
