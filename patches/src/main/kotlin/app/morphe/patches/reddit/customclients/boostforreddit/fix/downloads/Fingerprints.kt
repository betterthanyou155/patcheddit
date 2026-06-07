/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.downloads

import app.morphe.patcher.Fingerprint

internal val downloadAudioFingerprint = Fingerprint(
    strings = listOf("/DASH_audio.mp4", "/audio")
)

internal val mediaActivityM1Fingerprint = Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/activities/MediaActivity;",
    name = "M1",
    parameters = listOf("Landroid/view/View;")
)

internal val mediaActivityZ1Fingerprint = Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/activities/MediaActivity;",
    name = "z1",
    parameters = listOf("Lcom/rubenmayayo/reddit/ui/customviews/menu/MenuOption;")
)

