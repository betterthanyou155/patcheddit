/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.welcome

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.boostforreddit.BoostCompatible
import app.morphe.patches.reddit.customclients.boostforreddit.misc.extension.sharedExtensionPatch
import app.morphe.util.indexOfFirstInstructionReversed
import com.android.tools.smali.dexlib2.Opcode

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/morphe/extension/boostforreddit/utils/WelcomeDialogUtils;"

internal val onCreateFingerprint = Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/submissions/subreddit/MainActivity;",
    name = "onCreate",
)

@Suppress("unused")
val welcomeDialogPatch = bytecodePatch(
    name = "Welcome Dialog Patch",
    default = true
) {
    dependsOn(sharedExtensionPatch)
    compatibleWith(*BoostCompatible)

    execute {
        onCreateFingerprint.method.apply {
            val index = indexOfFirstInstructionReversed(Opcode.RETURN_VOID)
            addInstructions(
                index,
                "invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->showWelcomeDialog(Landroid/app/Activity;)V"
            )
        }
    }
}
