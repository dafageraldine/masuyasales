package com.yusuffahrudin.masuyamobileapp.util

import com.yusuffahrudin.masuyamobileapp.BuildConfig

/**
 * Created by yusuf fahrudin on 16-02-2017.
 */
class Server(path: String?) {
    private var URL: String? = null
    private var URL_IMAGE: String? = null

    fun URL(): String {
        URL = if (path.equals("JKT", ignoreCase = true)) {
            "http://103.119.228.145:83/cobaApp2/masuyamobile$path/"
        } else if (path.equals("TES", ignoreCase = true)) {
            "http://91d70b4fe043.sn.mynetname.net:81/cobaApp2/masuyates/"
        } else {
            "http://91d70b4fe043.sn.mynetname.net:81/cobaApp2/masuyamobile$path/"
        }
        return URL.toString()
    }

    fun URL_IMAGE(): String {
        URL_IMAGE = if (path.equals("JKT", ignoreCase = true)) {
            "http://103.119.228.145:83/cobaApp2/Images/katalog/"
        } else if (path.equals("TES", ignoreCase = true)) {
            "http://91d70b4fe043.sn.mynetname.net:81/cobaApp2/Images/katalog/"
        } else {
            "http://91d70b4fe043.sn.mynetname.net:81/cobaApp2/Images/katalog/"
        }
        return URL_IMAGE.toString()
    }

    fun URL_IMAGE_PROFILE(): String {
        URL_IMAGE = if (path.equals("JKT", ignoreCase = true)) {
            "http://103.119.228.145:83/cobaApp2/masuyamobile$path/images/user/"
        } else if (path.equals("TES", ignoreCase = true)) {
            "http://91d70b4fe043.sn.mynetname.net:81/cobaApp2/masuyates/images/user/"
        } else {
            "http://91d70b4fe043.sn.mynetname.net:81/cobaApp2/masuyamobile$path/images/user/"
        }
        return URL_IMAGE.toString()
    }

    fun URL_APK(versi: String): String {
        URL = if (path.equals("JKT", ignoreCase = true)) {
            "http://103.119.228.145:83/cobaApp2/apk/masuya-v$versi.apk"
        } else {
            "http://91d70b4fe043.sn.mynetname.net:81/cobaApp2/apk/masuya-v$versi.apk"
        }
        return URL.toString()
    }

    companion object {
        private var path: String? = null
    }

    init {
        Companion.path = path
    }
}