# Hypercart - Application Android E-commerce

## 🚀 Améliorations Implémentées

### Architecture MVVM
- **Repository Pattern** : Séparation claire entre la couche de données et la logique métier
- **ViewModels** : Gestion d'état centralisée avec StateFlow
- **Composants réutilisables** : UI components modulaires et réutilisables

### Sécurité
- **Configuration sécurisée** : Clés API externalisées dans `config.properties`
- **Validation robuste** : Validateurs centralisés pour tous les formulaires
- **Gestion de session** : Persistance sécurisée avec DataStore

### Navigation
- **Routes typées** : Navigation avec sealed classes pour la sécurité des types
- **Deep linking** : Support des liens profonds pour la réinitialisation de mot de passe
- **Transitions animées** : Animations fluides entre les écrans

### UI/UX
- **Design System** : Composants cohérents et réutilisables
- **Accessibilité** : Support des contentDescription et navigation clavier
- **États de chargement** : Indicateurs visuels pour les opérations asynchrones
- **Gestion d'erreurs** : Dialogs informatifs et récupération d'erreurs

### Fonctionnalités
- **Authentification complète** : Email/password + Google Sign-In
- **Gestion des mots de passe** : Réinitialisation et mise à jour sécurisées
- **Liste de produits** : Interface moderne avec panier d'achat
- **Persistance de session** : Connexion automatique

## 📁 Structure du Projet

```
app/src/main/java/com/hypercart/
├── config/
│   └── SecureConfig.kt          # Configuration sécurisée
├── data/
│   ├── AuthRepository.kt        # Repository pour l'authentification
│   └── SessionManager.kt        # Gestionnaire de session
├── navigation/
│   └── NavRoutes.kt             # Routes de navigation typées
├── ui/
│   ├── components/
│   │   └── CommonComponents.kt  # Composants réutilisables
│   ├── screens/
│   │   ├── LoginScreen.kt       # Écran de connexion refactorisé
│   │   ├── RegisterScreen.kt    # Écran d'inscription refactorisé
│   │   └── ProductListScreen.kt # Liste de produits améliorée
│   └── viewmodel/
│       ├── LoginViewModel.kt    # ViewModel pour la connexion
│       └── RegisterViewModel.kt # ViewModel pour l'inscription
├── utils/
│   └── Validators.kt            # Validateurs centralisés
└── [Fichiers existants...]
```

## 🔧 Configuration

### 1. Configuration sécurisée
Créez le fichier `app/src/main/assets/config.properties` :
```properties
SUPABASE_URL=votre_url_supabase
SUPABASE_KEY=votre_clé_supabase
GOOGLE_CLIENT_ID=votre_client_id_google
RESET_PASSWORD_REDIRECT_URL=votre_url_redirect
```

### 2. Dépendances ajoutées
```kotlin
// DataStore pour la persistance
implementation("androidx.datastore:datastore-preferences:1.0.0")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.6")
```

## 🎯 Fonctionnalités Principales

### Authentification
- ✅ Connexion par email/mot de passe
- ✅ Connexion Google
- ✅ Inscription avec validation
- ✅ Réinitialisation de mot de passe
- ✅ Persistance de session

### Interface Utilisateur
- ✅ Design moderne et cohérent
- ✅ Composants réutilisables
- ✅ Animations fluides
- ✅ Gestion des états de chargement
- ✅ Messages d'erreur informatifs

### Sécurité
- ✅ Validation côté client
- ✅ Configuration sécurisée
- ✅ Gestion d'erreurs robuste
- ✅ Session sécurisée

## 🚀 Utilisation

1. **Cloner le projet**
2. **Configurer les clés API** dans `config.properties`
3. **Synchroniser les dépendances**
4. **Lancer l'application**

## 📱 Écrans Disponibles

- **Login** : Connexion avec email/password ou Google
- **Register** : Inscription avec validation
- **Reset Password** : Réinitialisation de mot de passe
- **Products** : Liste des produits avec panier
- **New Password** : Définition d'un nouveau mot de passe

## 🔒 Sécurité

- Configuration externalisée
- Validation robuste des entrées
- Gestion sécurisée des sessions
- Messages d'erreur informatifs
- Pas de données sensibles en dur

## 🎨 Design System

- **Couleurs** : Palette cohérente (noir, bleu, gris)
- **Typographie** : Material Design 3
- **Composants** : Boutons, champs, dialogs réutilisables
- **Animations** : Transitions fluides entre écrans

## 📈 Améliorations Futures

- [ ] Tests unitaires et d'intégration
- [ ] Mode sombre/clair
- [ ] Support multilingue
- [ ] Notifications push
- [ ] Paiement intégré
- [ ] Historique des commandes
- [ ] Profil utilisateur
- [ ] Recherche de produits
- [ ] Filtres et tri
- [ ] Wishlist

## 🤝 Contribution

1. Fork le projet
2. Créer une branche feature
3. Commiter les changements
4. Pousser vers la branche
5. Ouvrir une Pull Request

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier LICENSE pour plus de détails. 