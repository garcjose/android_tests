package com.basic.appkiller

import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.basic.appkiller.ui.theme.AppKillerTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val packages = mutableSetOf<String>();

        val currentTime = System.currentTimeMillis()
        val usageEvents = usm.queryEvents(currentTime - (1000 * 60 * 10), currentTime)
        val usageEvent = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(usageEvent)
//            Log.i(
//                TAG, "${usageEvent.packageName} ${usageEvent.eventType} ${usageEvent.timeStamp}"
//            )
            if (usageEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                packages.add(usageEvent.packageName)
            }
        }
        // add most common packages
        packages.add("com.google.android.youtube");
        packages.add("com.reddit.frontpage");
        packages.add("com.android.chrome");
        packages.add("com.whatsapp");
        packages.add("com.spotify.music");
        packages.add("com.ivoox.app");
        packages.add("tunein.player");

        // short check is possible to get app name
        for (pk3 in packages) {
            val appInfo = packageManager.getApplicationInfo(pk3, 0);
            val appName = packageManager.getApplicationLabel(appInfo);
            var isRunning=false; // runningAppProcesses only returns the current app..
            for(app in am.runningAppProcesses)
            {
                if(pk3 in app.pkgList)
                {
                    isRunning = true;
                    break;
                }
            }
            Log.i(
                TAG, "${pk3} ${appName} ${isRunning}"
            )
        }

        setContent {
            AppKillerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    AppPackageKiller(am, packages.toList())
                }
            }
        }
    }
}

@Composable
fun AppKiller(
    am: ActivityManager, modifier: Modifier = Modifier
) {
    AppView(am, am.runningAppProcesses, modifier)
}


@Composable
fun AppView(
    am: ActivityManager?,
    appList: List<ActivityManager.RunningAppProcessInfo>,
    modifier: Modifier = Modifier
) {
    Column {
        Text(
            text = "Kill apps", modifier = modifier, style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = modifier.height(8.dp))
        AppList(am, appList, modifier)
    }
}

@Composable
fun AppList(
    am: ActivityManager?,
    appList: List<ActivityManager.RunningAppProcessInfo>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(appList) { app ->
            AppCard(
                app = app, onAppClick = {
                    for (pkg in app.pkgList) {
                        Log.d(TAG, "killing: $pkg")
                        am?.killBackgroundProcesses(pkg)
                    }
                    Log.d(TAG, "killing: $app.pid")
                    Process.killProcess(app.pid)
                }, modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun AppCard(
    app: ActivityManager.RunningAppProcessInfo,
    onAppClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column {
            TextButton(
                onClick = onAppClick,
                shape = RoundedCornerShape(40.dp),
                modifier = modifier,
            ) {
                Text(text = app.processName, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}


@Composable
fun AppPackageKiller(
    am: ActivityManager, packageList: List<String>, modifier: Modifier = Modifier
) {
    AppPackageView(am, packageList, modifier)
}

@Composable
fun AppPackageView(
    am: ActivityManager?, packageList: List<String>, modifier: Modifier = Modifier
) {
    Column {
        Text(
            text = "Kill apps", modifier = modifier, style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = modifier.height(8.dp))
        PackageList(am, packageList, modifier)
    }
}

@Composable

fun PackageList(
    am: ActivityManager?, packageList: List<String>, modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LazyColumn(modifier = modifier) {
        items(packageList) { packageName ->
            PackageCard(
                packageName = packageName, onPackageClick = {
                    Log.d(TAG, "killing: $packageName")
                    am?.killBackgroundProcesses(packageName)

                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:$packageName"),
                    );
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_CLEAR_TOP //or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent, null)
                    //val intent2 = Intent(Settings.ACTION_APPLICATION_SETTINGS)
                }, modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun PackageCard(
    packageName: String, onPackageClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column {
            TextButton(
                onClick = onPackageClick,
                shape = RoundedCornerShape(40.dp),
                modifier = modifier,
            ) {
                Text(text = packageName, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppKillerPreview() {
    AppKillerTheme {
        AppView(null, listOf(ActivityManager.RunningAppProcessInfo("pname", 50, arrayOf("nope"))))
    }
}