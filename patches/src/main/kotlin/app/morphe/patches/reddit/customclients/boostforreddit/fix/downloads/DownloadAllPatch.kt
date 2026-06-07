/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.downloads

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
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
        //
        // ROOT CAUSE OF ALL PREVIOUS CRASHES:
        // z1 has .locals 0 (no local registers). Any addInstructionsWithLabels call that
        // introduces a new v-register bumps .locals from 0 to 1, which SHIFTS the absolute
        // register indices of p0 and p1 by +1. The patcher does NOT remap existing instructions,
        // so the original code starts using the wrong registers → VerifyError at runtime.
        //
        // THE FIX:
        // Use replaceInstruction to swap the first instruction (invoke-virtual q()I → int result)
        // with our void static dispatch. Then insert return-void right after. This adds ZERO new
        // registers — .locals stays 0 — and p1 is always seen as MenuOption by the verifier.
        // The rest of z1 (move-result, sparse-switch, handlers) becomes dead code after return-void.
        mediaActivityZ1Fingerprint.method.apply {
            // Replace: invoke-virtual {p1}, MenuOption->q()I
            // With:    invoke-static  {p0, p1}, handleAndDispatch(Activity, Object)V
            replaceInstruction(
                0,
                "invoke-static { p0, p1 }, $EXTENSION_CLASS_DESCRIPTOR->handleAndDispatch(Landroid/app/Activity;Ljava/lang/Object;)V"
            )
            // Insert return-void at index 1 (before the original move-result p1).
            // Dead code after here is fine — the verifier/ART handles it gracefully.
            addInstruction(
                1,
                "return-void"
            )
        }
    }
}
