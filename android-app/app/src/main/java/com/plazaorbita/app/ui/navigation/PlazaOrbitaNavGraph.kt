package com.plazaorbita.app.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.plazaorbita.app.ui.admin.AdminHomeScreen
import com.plazaorbita.app.ui.auth.LoginScreen
import com.plazaorbita.app.ui.auth.RegisterScreen
import com.plazaorbita.app.ui.business.BusinessHomeScreen
import com.plazaorbita.app.ui.customer.AppointmentBookingScreen
import com.plazaorbita.app.ui.customer.CustomerHomeScreen
import com.plazaorbita.app.ui.customer.ProductOrderScreen
import com.plazaorbita.app.ui.customer.BusinessDetailScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ADMIN_HOME = "admin_home"
    const val BUSINESS_HOME = "business_home"

    const val BUSINESS_DETAIL = "business_detail"
    const val CUSTOMER_HOME = "customer_home"
    const val PRODUCT_ORDER = "product_order"
    const val APPOINTMENT_BOOKING = "appointment_booking"
}

@Composable
fun PlazaOrbitaNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { role -> navigateByRole(navController, role) },
                onGoToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { role -> navigateByRole(navController, role) },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_HOME) { AdminHomeScreen() }

        // businessId=1L es un placeholder: en el siguiente sprint se obtiene
        // el negocio real ligado al usuario dueño (GET /api/businesses?ownerId=...)
        composable(Routes.BUSINESS_HOME) { BusinessHomeScreen(businessId = 1L) }

        composable(
            route = "${Routes.CUSTOMER_HOME}/{startTab}",
            arguments = listOf(navArgument("startTab") { type = NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            val startTab = backStackEntry.arguments?.getInt("startTab") ?: 0
            CustomerHomeScreen(navController = navController, startTab = startTab)
        }

        composable(
            route = "${Routes.BUSINESS_DETAIL}/{businessId}",
            arguments = listOf(navArgument("businessId") { type = NavType.LongType })
        ) { backStackEntry ->
            val businessId = backStackEntry.arguments?.getLong("businessId") ?: 0L
            BusinessDetailScreen(businessId = businessId, navController = navController)
        }

        composable(
            route = "${Routes.PRODUCT_ORDER}/{businessId}/{businessName}",
            arguments = listOf(
                navArgument("businessId") { type = NavType.LongType },
                navArgument("businessName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val businessId = backStackEntry.arguments?.getLong("businessId") ?: 0L
            val businessName = Uri.decode(backStackEntry.arguments?.getString("businessName") ?: "")
            ProductOrderScreen(businessId = businessId, businessName = businessName, navController = navController)
        }

        composable(
            route = "${Routes.APPOINTMENT_BOOKING}/{businessId}/{businessName}/{service}",
            arguments = listOf(
                navArgument("businessId") { type = NavType.LongType },
                navArgument("businessName") { type = NavType.StringType },
                navArgument("service") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val businessId = backStackEntry.arguments?.getLong("businessId") ?: 0L
            val businessName = Uri.decode(backStackEntry.arguments?.getString("businessName") ?: "")
            val serviceRaw = Uri.decode(backStackEntry.arguments?.getString("service") ?: "none")
            val presetServiceName = if (serviceRaw == "none") null else serviceRaw
            AppointmentBookingScreen(
                businessId = businessId,
                businessName = businessName,
                presetServiceName = presetServiceName,
                navController = navController
            )
        }
    }
}

private fun navigateByRole(navController: NavHostController, role: String) {
    val destination = when (role) {
        "ADMIN" -> Routes.ADMIN_HOME
        "BUSINESS_OWNER" -> Routes.BUSINESS_HOME
        else -> "${Routes.CUSTOMER_HOME}/0"
    }
    navController.navigate(destination) {
        popUpTo(Routes.LOGIN) { inclusive = true }
    }
}