package com.example.tsuappmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.maplibre.android.MapLibre
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.example.tsuappmap.map.CampusGrid
import com.example.tsuappmap.map.CampusMapView
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        CampusGrid.load(this)
        setContent {
            TsuMapScreen()
        }
    }
}
@Composable
fun TsuMapScreen(vm : MainViewModel = viewModel()) {
    val context = LocalContext.current

    BottomSheetScaffold(
        sheetContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(-30.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    TabButton("Построить маршрут (А*)",{ vm.selectedTab = 1}, 12)
                    TabButton("Кластеризация", { vm.selectedTab = 2 }, 12)
                    TabButton("Приобрести еду", { vm.selectedTab = 3 }, 12)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    TabButton("Выбор и обход достоприм.", { vm.selectedTab = 4 }, 12)
                    TabButton("Дерево решений", { vm.selectedTab = 5}, 12)
                    TabButton("Кнопка 6", { vm.selectedTab = 6 }, 12)
                }

                TabContent(
                    selectedTab = vm.selectedTab,
                    isObstacleMode = vm.isObstacleMode,
                    onPlaceStart = {vm.onPlaceStartClicked(context)},
                    onPlaceEnd = {vm.onPlaceEndClicked(context)},
                    onToggleObstacle = {vm.onToggleObstacle(context)},
                    onShowManhattan = {vm.showManhattanClusters(context)},
                    onShowEuclidian ={vm.showEuclideanClusters(context)},
                    onPlaceAntStart = {vm.onPlaceAntStart(context)},
                    onRunAntColony = {indices -> vm.runAntColony(indices, context)},
                    antStartSet = vm.antStartSet,
                    onClearMap = {vm. clearMap()},
                    clearCounter = vm.clearCounter,
                    onShowDecisionTree = { vm.showDecisionTree = true }
                )
            }
        },
        sheetPeekHeight = 56.dp,
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle(
                color = ComposeColor.Black,
                width = 60.dp,
                height = 5.dp
            )
        }
    )  {
        Box(modifier = Modifier.fillMaxSize().background(ComposeColor.Black)) {

            CampusMapView(
                onMapReady = { map ->
                    vm.mapRef = map
                    map.addOnMapClickListener { latLng -> vm.onMapClick(latLng, map, context) }
                },

                modifier = Modifier.fillMaxSize()
            )


            Image(
                painter = painterResource(id = R.drawable.hits),
                contentDescription = null,
                modifier = Modifier.size(50.dp).align(Alignment.TopStart).padding(8.dp)
            )
        }
    }

    if (vm.showDecisionTree) {
        DecisionTreeModal(onDismiss = { vm.showDecisionTree = false } )
    }
}