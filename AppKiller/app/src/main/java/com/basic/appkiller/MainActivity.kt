package com.basic.appkiller

import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
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
            Log.i(
                "APP",
                "${usageEvent.packageName} ${usageEvent.eventType} ${usageEvent.timeStamp}"
            )
            if (usageEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                val packageInfo=packageManager.getPackageInfo(usageEvent.packageName,0)
                val processName=packageInfo.applicationInfo.processName;
                packages.add(usageEvent.packageName)
            }
        }
// global settings
        val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
        //startActivity(intent)

        val intentSpecific = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.google.android.youtube" ));
        startActivity(intentSpecific)

        setContent {
            AppKillerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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
            text = "Kill apps",
            modifier = modifier,
            style = MaterialTheme.typography.headlineSmall
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
                app = app,
                onAppClick = {
                    for (pkg in app.pkgList) {
                        Log.d(TAG, "killing: $pkg")
                        am?.killBackgroundProcesses(pkg)
                    }
                    Log.d(TAG, "killing: $app.pid")
                    Process.killProcess(app.pid)
                },
                modifier = Modifier.padding(8.dp)
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
            )
            {
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
    am: ActivityManager?,
    packageList: List<String>,
    modifier: Modifier = Modifier
) {
    Column {
        Text(
            text = "Kill apps",
            modifier = modifier,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = modifier.height(8.dp))
        PackageList(am, packageList, modifier)
    }
}

@Composable
fun PackageList(
    am: ActivityManager?,
    packageList: List<String>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(packageList) { packageName ->
            PackageCard(
                packageName = packageName,
                onPackageClick = {
                    Log.d(TAG, "killing: $packageName")
                    am?.killBackgroundProcesses(packageName)

                },
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun PackageCard(
    packageName: String,
    onPackageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column {
            TextButton(
                onClick = onPackageClick,
                shape = RoundedCornerShape(40.dp),
                modifier = modifier,
            )
            {
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