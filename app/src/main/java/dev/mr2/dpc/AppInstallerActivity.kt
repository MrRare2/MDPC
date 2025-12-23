package dev.mr2.dpc

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mr2.dpc.ui.AppInstaller
import dev.mr2.dpc.ui.theme.MDPCTheme

class AppInstallerActivity:FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val vm by viewModels<AppInstallerViewModel>()
        vm.initialize(intent)
        vm.registerInstallerReceiver(this)

        setContent {
            val theme by vm.theme.collectAsStateWithLifecycle()

            MDPCTheme(theme) {
                val uiState by vm.uiState.collectAsState()
                AppInstaller(
                    uiState, vm::onPackagesAdd, vm::onPackageRemove, vm::startInstall, vm::closeResultDialog
                )
            }
        }
    }
}
