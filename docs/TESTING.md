# Tests Hypercart

Ce document décrit la stratégie de test complète pour l'application Hypercart.

## Structure des Tests

### Tests Unitaires (`/src/test/`)
- **AuthManagerTest.kt** : Tests pour la logique d'authentification
- **ValidationUtilsTest.kt** : Tests pour les fonctions de validation
- **SupabaseIntegrationTest.kt** : Tests d'intégration avec Supabase

### Tests UI (`/src/androidTest/`)
- **LoginScreenTest.kt** : Tests de l'interface de connexion
- **RegisterScreenTest.kt** : Tests de l'interface d'inscription
- **DialogAlertTest.kt** : Tests des dialogs d'alerte
- **PasswordGestionTest.kt** : Tests de gestion des mots de passe
- **NavigationIntegrationTest.kt** : Tests de navigation entre écrans

## Configuration Gradle

Ajoutez ces dépendances à votre `build.gradle.kts` (app) :

```kotlin
dependencies {
    // Tests unitaires
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("io.mockk:mockk:1.13.7")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.robolectric:robolectric:4.10.3")

    // Tests UI
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_version")
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")
    
    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling:$compose_version")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$compose_version")
}
```

## Exécution des Tests

### Tests Unitaires
```bash
./gradlew test
```

### Tests UI
```bash
./gradlew connectedAndroidTest
```

### Tests Spécifiques
```bash
# Test d'une classe spécifique
./gradlew testDebugUnitTest --tests="com.hypercart.AuthManagerTest"

# Test d'une méthode spécifique
./gradlew testDebugUnitTest --tests="com.hypercart.AuthManagerTest.signUpWithEmail_returns_error_for_short_password"
```

## Couverture de Tests

### Fonctionnalités Testées

#### Authentification
- ✅ Validation des mots de passe
- ✅ Validation des emails
- ✅ Gestion des erreurs Supabase
- ✅ Connexion Google
- ✅ Inscription par email

#### Interface Utilisateur
- ✅ Affichage des écrans
- ✅ Validation des formulaires
- ✅ Navigation entre écrans
- ✅ Dialogs d'erreur et de succès

#### Intégration
- ✅ Configuration Supabase
- ✅ Navigation avec paramètres
- ✅ Deep links

## Types de Tests

### 1. Tests Unitaires
- Testent la logique métier isolée
- Utilisent des mocks pour les dépendances
- Rapides à exécuter

### 2. Tests d'Intégration
- Testent l'interaction entre composants
- Vérifient les configurations
- Testent la navigation

### 3. Tests UI
- Testent l'interface utilisateur
- Utilisent Compose Test
- Simulent les interactions utilisateur

## Bonnes Pratiques

### Nomenclature
- Nom de test : `methodName_condition_expectedResult`
- Exemple : `signUpWithEmail_shortPassword_returnsError`

### Structure AAA
```kotlin
@Test
fun test_name() {
    // Arrange - Préparer les données
    val input = "test data"
    
    // Act - Exécuter l'action
    val result = functionToTest(input)
    
    // Assert - Vérifier le résultat
    assertEquals(expected, result)
}
```

### Tests Compose
```kotlin
@Test
fun component_condition_behavior() {
    composeTestRule.setContent {
        ComponentToTest()
    }
    
    composeTestRule.onNodeWithText("Text").assertIsDisplayed()
    composeTestRule.onNodeWithText("Button").performClick()
    composeTestRule.onNodeWithText("Result").assertExists()
}
```

## CI/CD

Pour l'intégration continue, ajoutez dans `.github/workflows/android.yml` :

```yaml
- name: Run unit tests
  run: ./gradlew test

- name: Run UI tests
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 29
    script: ./gradlew connectedAndroidTest
```

## Métriques de Qualité

### Objectifs de Couverture
- Tests unitaires : > 80%
- Tests UI : > 60%
- Fonctionnalités critiques : 100%

### Outils d'Analyse
- JaCoCo pour la couverture de code
- Detekt pour l'analyse statique
- Lint pour les bonnes pratiques Android

## Maintenance

### Mise à Jour des Tests
- Maintenir les tests à jour avec les changements de code
- Ajouter des tests pour les nouvelles fonctionnalités
- Refactoriser les tests obsolètes

### Performance
- Optimiser les tests lents
- Utiliser des mocks appropriés
- Paralléliser quand possible