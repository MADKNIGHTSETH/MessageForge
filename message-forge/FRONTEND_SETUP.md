# MessageForge - Prototype Frontend Vue 3 + Pinia

## Vue d'ensemble

**MessageForge** est une plateforme multi-canal de messagerie intelligente. Ce dépôt contient le prototype frontend complètement fonctionnel, construit avec Vue 3, Vite, Pinia et Tailwind CSS.

### Architecture

```
Frontend (Vue 3 + Pinia)
├── Authentification (Mock)
├── Éditeur multi-canal
├── Prévisualisation temps réel
├── Simulation d'envoi
└── Historique persistant (localStorage)

Base de données (Supabase + MongoDB - Phase future)
├── users (authentification)
└── messages (pont relationnel)
```

## Fonctionnalités implémentées

### 1. Authentification mock
- Pages de login et d'inscription  
- Génération de JWT simulé
- Session utilisateur stockée en localStorage
- Route guards pour pages protégées

### 2. Éditeur de message multi-canal
- Sélection dynamique des canaux (Email, SMS, LinkedIn, Slack, X, etc.)
- Champ objet (visible uniquement pour Email)
- Compteur de caractères en temps réel
- Décorateurs : Emoji, Hashtag, Signature

### 3. Prévisualisation temps réel
- Debounced à 220ms pour performance
- Formatage spécifique par canal :
  - **Email** : HTML avec en-tête/pied de page, objet limité à 60 caractères
  - **SMS** : Texte brut tronqué à 155 caractères
  - **LinkedIn** : 3 premiers mots en majuscules, paragraphes courts
  - **Slack** : Pseudo-Markdown (gras, code)
  - **X/Twitter** : Limite stricte à 270 caractères
- Affichage instantané des avertissements

### 4. Simulation d'envoi
- Progression séquentielle par canal (700ms par canal)
- Statut PENDING → SENT/FAILED
- Simulation d'erreur pour Messenger
- Barre de progression visuelle

### 5. Historique persistant
- Stockage en localStorage via Pinia
- Affichage de l'historique des envois
- Métadonnées (date, canaux, statut)

## Structure des dossiers

```
src/
├── components/
│   ├── compose/
│   │   └── MessageEditor.vue      # Composant principal
│   └── shared/
│       └── SectionCard.vue        # Wrapper partagé
├── stores/
│   ├── auth.js                    # Authentification
│   ├── message.js                 # Gestion des messages
│   ├── preview.js                 # Génération de prévisualisations
│   └── channel.js                 # Canaux disponibles
├── views/
│   ├── compose/
│   │   └── ComposeView.vue        # Page de composition
│   ├── auth/
│   │   ├── LoginView.vue          # Page de connexion
│   │   └── RegisterView.vue       # Page d'inscription
│   ├── history/
│   │   └── HistoryView.vue        # Historique des envois
│   └── settings/
│       └── SettingsView.vue       # Paramètres (placeholder)
├── services/
│   └── mockApi.js                 # Utilitaires de simulation
├── router/
│   └── index.js                   # Configuration Vue Router
└── styles/
    └── index.css                  # Tailwind CSS
```

## Démarrage

### Prérequis
- Node.js >= 20.19.0
- npm >= 10

### Installation

```bash
cd message-forge
npm install
```

### Développement

```bash
npm run dev
```

Accédez à `http://localhost:5176` et naviguez vers `/register` ou `/login`.

### Build

```bash
npm run build
```

### Démonstration des credentials

Pour tester, vous pouvez créer un compte avec :
- Email : `demo@messageforge.local`
- Mot de passe : n'importe quel mot de passe
- Nom : n'importe quel nom

## Points clés du design

### Authentification

L'authentification utilise un mock JWT stocké en localStorage :
```javascript
const token = createJwt({ userId: '...', email: '...' })
// Token format : "mock.BASE64_HEADER.BASE64_PAYLOAD"
```

### State Management

Les stores Pinia gèrent l'état de manière isolée :
- `useAuthStore` : session utilisateur
- `useMessageStore` : brouillons, historique, envois
- `usePreviewStore` : prévisualisations générées
- `useChannelStore` : configuration des canaux

### Persistance

Les données sont persistées dans `localStorage` par clé :
- `messageforge_auth` : session utilisateur
- `messageforge_messages` : brouillons et historique
- `messageforge_channels` : configuration des canaux

### Formatage de prévisualisation

Le module `mockApi.js` implémente la logique de formatage :
```javascript
formatPreview(rawText, channel, decorators)
```

Chaque canal a ses propres règles de formatage.

## Flux utilisateur

1. **Inscription/Connexion** → `/register` ou `/login`
2. **Composition** → `/compose`
   - Écrire le texte brut
   - Activer les canaux
   - Observer les prévisualisations en temps réel
   - Cocher les décorateurs
3. **Envoi** → Simulation avec progression par canal
4. **Historique** → `/history` pour voir les envois passés

## Notes sur l'architecture future

### Supabase Integration

Les tables PostgreSQL sont définies dans [SUPABASE_DDL.sql](./SUPABASE_DDL.sql) :
- `users` : gestion complète des utilisateurs
- `messages` : pont relationnel avec `user_id` (FK)

### MongoDB Integration

Les détails enrichis des messages seront stockés dans MongoDB :
```javascript
{
  messageId: "uuid",
  userId: "uuid",
  subject: "...",
  rawContent: "...",
  activeChannels: ["Email", "SMS"],
  decorators: { emoji: true, signature: true },
  status: "SENT",
  sentAt: "2026-05-21T22:22:48Z"
}
```

## Technologies utilisées

- **Vue 3** : Framework UI (Composition API + `<script setup>`)
- **Vite** : Bundler rapide
- **Pinia** : State management
- **Vue Router** : Routage client
- **Tailwind CSS** : Styling utility-first
- **@lucide/vue** : Icônes minimalistes
- **localStorage** : Persistance locale

## Limitations actuelles (Mock)

1. Pas de backend réel : tout est simulé localement
2. Pas de WebSocket/STOMP : envoi simulé avec `setInterval`
3. Pas de base de données : localStorage uniquement
4. Authentification factice : JWT mock, pas de vérification réelle

Ces limitations seront levées lors de l'intégration avec Supabase et MongoDB.

## Contribuer

1. Fork le dépôt
2. Créez une branche feature (`git checkout -b feature/nouvelle-feature`)
3. Commitez vos changements
4. Poussez vers la branche (`git push origin feature/nouvelle-feature`)
5. Ouvrez une Pull Request

## Licence

MIT
