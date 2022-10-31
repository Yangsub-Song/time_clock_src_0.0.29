package com.techrove.timeclock.controller.model

/**
 * 버전 정보
 */
data class Version(val major: Int, val minor: Int, val patch: Int): Comparable<Version> {
    override fun compareTo(other: Version): Int {
        if (major != other.major) {
            return major - other.major
        }
        if (minor != other.minor) {
            return minor - other.minor
        }
        if (patch != other.patch) {
            return patch - other.patch
        }
        return 0
    }

    override fun toString(): String {
        return "$major.$minor.$patch"
    }

    companion object {
        fun parse(versionString: String): Version {
            val regex =
                if (versionString.contains("_")) """.+_(\d+).(\d+)\.(\d+)_.+""" else """(\d+).(\d+)\.(\d+)"""
            regex.toRegex().find(versionString)?.groupValues?.takeIf { it.size == 4 }
                ?.let { values ->
                    val major = values[1].toInt()
                    val minor = values[2].toInt()
                    val patch = values[3].toInt()
                    return Version(major, minor, patch)
                }
            return Version(0, 0, 0)
        }
    }
}
