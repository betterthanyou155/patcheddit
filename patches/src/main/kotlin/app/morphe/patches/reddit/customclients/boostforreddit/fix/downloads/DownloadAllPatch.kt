/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.downloads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.morphe.patches.reddit.customclients.boostforreddit.BoostCompatible
import app.morphe.patches.reddit.customclients.boostforreddit.misc.extension.sharedExtensionPatch
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/morphe/extension/boostforreddit/utils/DownloadAllUtils;"

@Suppress("unused")
val downloadAllPatch = bytecodePatch(
    name = "Download All option in media activity",
    description = "Adds a 'Download All' option next to standard download options for posts with multiple images/media.",
    default = true
) {
    dependsOn(sharedExtensionPatch)
    compatibleWith(*BoostCompatible)

    execute {
        // Patch MediaActivity.M1 to intercept bottom sheet menu initialization and add our custom option.
        mediaActivityM1Fingerprint.method.apply {
            val index = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_VIRTUAL &&
                    getReference<MethodReference>()?.let { ref ->
                        ref.name == "J1" && ref.definingClass == "Lcom/rubenmayayo/reddit/ui/activities/MediaActivity;"
                    } == true
            }
            replaceInstruction(
                index,
                "invoke-static { p0, p1 }, $EXTENSION_CLASS_DESCRIPTOR->showMenu(Landroid/app/Activity;Ljava/util/List;)V"
            )
        }

        // Patch MediaActivity.z1 to handle clicks on the custom option.
        mediaActivityZ1Fingerprint.method.apply {
            addInstructionsWithLabels(
                0,
                """
                    invoke-static { p0, p1 }, $EXTENSION_CLASS_DESCRIPTOR->handleMenuClick(Landroid/app/Activity;Ljava/lang/Object;)Ljava/lang/Object;
                    move-result-object p1
                    if-nez p1, :continue
                    return-void
                    :continue
                    check-cast p1, Lcom/rubenmayayo/reddit/ui/customviews/menu/MenuOption;
                """
            )
        }
    }
}
