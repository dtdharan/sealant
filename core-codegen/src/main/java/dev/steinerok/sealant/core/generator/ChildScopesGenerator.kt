package dev.steinerok.sealant.core.generator

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.anvil.compiler.internal.reference.classAndInnerClassReferences
import com.squareup.anvil.compiler.internal.reference.generateClassName
import com.squareup.anvil.compiler.internal.safePackageString
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import dev.steinerok.sealant.core.ClassSpec
import dev.steinerok.sealant.core.ConstructorSpec
import dev.steinerok.sealant.core.FqNames
import dev.steinerok.sealant.core.SealantFeature
import dev.steinerok.sealant.core.buildFile
import dev.steinerok.sealant.core.buildVmScopeClassName
import dev.steinerok.sealant.core.hasSealantFeatureForScope
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

/**
 * Should generate:
 * ```
 * public abstract class ViewModel_<Scope> private constructor()
 * ```
 */
@AutoService(CodeGenerator::class)
public class ChildScopesGenerator : AlwaysApplicableCodeGenerator {

    override fun generateCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        projectFiles: Collection<KtFile>
    ): Collection<GeneratedFile> = projectFiles
        .classAndInnerClassReferences(module)
        .filter { clazz ->
            clazz.isAnnotatedWith(FqNames.sealantConfiguration)
        }
        .mapNotNull { generateScopes(codeGenDir, it) }
        .toList()

    private fun generateScopes(codeGenDir: File, clazz: ClassReference): GeneratedFile? {
        val hasViewModelSupport = clazz.hasSealantFeatureForScope(SealantFeature.ViewModel)
        if (!hasViewModelSupport) return null
        //
        val packageName = clazz.packageFqName.safePackageString(dotSuffix = false)
        val fileName =
            clazz.generateClassName().relativeClassName.asString() + "_SealantChildScopes"
        val origClassName = clazz.asClassName()
        //
        val content = FileSpec.buildFile(packageName, fileName) {
            //
            val vmScopeClassName = buildVmScopeClassName(origClassName)
            val vmScopeClass = ClassSpec(vmScopeClassName) {
                addModifiers(KModifier.ABSTRACT)
                primaryConstructor(
                    ConstructorSpec {
                        addModifiers(KModifier.PRIVATE)
                    }
                )
            }
            addType(vmScopeClass)
        }
        return createGeneratedFile(codeGenDir, packageName, fileName, content)
    }
}