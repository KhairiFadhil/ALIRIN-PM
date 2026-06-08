package com.example.alirinmobile.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.ui.graphics.vector.ImageVector

/** Maps the design's "name" tokens to Compose Material icons. */
object AlirinIcons {
    val home          = Icons.Outlined.Home
    val map           = Icons.Outlined.Map
    val pin           = Icons.Outlined.Place
    val clock         = Icons.Outlined.AccessTime
    val info          = Icons.Outlined.Info
    val close         = Icons.Outlined.Close
    val arrowRight    = Icons.AutoMirrored.Outlined.ArrowForward
    val arrowLeft     = Icons.AutoMirrored.Outlined.ArrowBack
    val chevronRight  = Icons.AutoMirrored.Outlined.KeyboardArrowRight
    val chevronDown   = Icons.Outlined.KeyboardArrowDown
    val check         = Icons.Outlined.Check
    val camera        = Icons.Outlined.CameraAlt
    val image         = Icons.Outlined.Image
    val bolt          = Icons.Outlined.Bolt
    val document      = Icons.Outlined.Description
    val filter        = Icons.Outlined.FilterList
    val search        = Icons.Outlined.Search
    val layers        = Icons.Outlined.Layers
    val wifi          = Icons.Outlined.Wifi
    val wifiOff       = Icons.Outlined.WifiOff
    val droplet       = Icons.Outlined.WaterDrop
    val trash         = Icons.Outlined.DeleteOutline
    val cloud         = Icons.Outlined.Cloud
    val sensor        = Icons.Outlined.Sensors
    val history       = Icons.Outlined.History
    val share         = Icons.Outlined.IosShare
    val shield        = Icons.Outlined.Shield
    val users         = Icons.Outlined.PeopleOutline
    val sparkles      = Icons.Outlined.AutoAwesome
    val bell          = Icons.Outlined.NotificationsNone
    val plus          = Icons.Outlined.Add
    val eye           = Icons.Outlined.Visibility
    val eyeOff        = Icons.Outlined.VisibilityOff

    fun byName(name: String): ImageVector = when (name) {
        "home" -> home; "map" -> map; "pin" -> pin; "clock" -> clock; "info" -> info
        "x" -> close; "arrow-right" -> arrowRight; "arrow-left" -> arrowLeft
        "chevron-right" -> chevronRight; "chevron-down" -> chevronDown; "check" -> check
        "camera" -> camera; "image" -> image; "bolt" -> bolt; "document" -> document
        "filter" -> filter; "search" -> search; "layers" -> layers; "wifi" -> wifi
        "wifi-off" -> wifiOff; "droplet" -> droplet; "trash" -> trash; "cloud" -> cloud
        "sensor" -> sensor; "history" -> history; "share" -> share; "shield" -> shield
        "users" -> users; "sparkles" -> sparkles; "bell" -> bell; "plus" -> plus
        "eye" -> eye; "eye-off" -> eyeOff
        else -> info
    }
}
