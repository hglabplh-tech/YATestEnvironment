package io.github.hglabplh_tech.reflect.reflkotlin
import kotlin.Metadata
import kotlin.metadata.jvm.KotlinClassMetadata

class ScalaTastyLikeKTReflect {
    /*TODO: rewrite to real object*/


    public fun inspectMetadata(
        clazz: Class<*>
    ): Any? {

        val metadata =
            clazz.getAnnotation(
                Metadata::class.java
            ) ?: return null

        val parsed =
            KotlinClassMetadata.readLenient(
                metadata
            )

        return when (parsed) {

            is KotlinClassMetadata.Class -> {
                    parsed.kmClass

            }

            is KotlinClassMetadata.FileFacade -> {
                    parsed.kmPackage
            }

            is KotlinClassMetadata.MultiFileClassPart -> {
                    parsed.kmPackage
            }

            else -> null
        }
    }
}