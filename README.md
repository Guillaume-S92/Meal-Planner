# Meal Planner

Application MVP de planification de repas basee sur le cahier des charges joint.

## Contenu

- `backend/` : API Spring Boot REST, JPA, Bean Validation, Liquibase, OpenAPI.
- `frontend/` : application Angular responsive avec Angular Material.
- Interface web statique de secours servie par Spring Boot dans `backend/src/main/resources/static`.
- Configuration MySQL par defaut sur `localhost:3306` avec `root/root`.
- Profil local H2 disponible en secours avec `SPRING_PROFILES_ACTIVE=local`.
- Changelog Liquibase inclus. Le build local utilise JPA `update` par defaut pour rester executable sans telechargement ; activer le profil Maven/Spring `liquibase` quand `liquibase-core` est disponible.

## Demarrage rapide

### Backend

```powershell
cd backend
mvn test
mvn -Pboot-plugin spring-boot:run
```

Puis ouvrir :

- Application : http://localhost:8080
- API OpenAPI : http://localhost:8080/swagger-ui.html

### Frontend Angular

Dans un second terminal :

```powershell
cd frontend
npm install
npm start
```

Puis ouvrir :

- Application Angular : http://localhost:4200

Le serveur Angular utilise `proxy.conf.json` pour appeler l'API Spring Boot sur `http://localhost:8080`.

## Base de donnees

Par defaut, le backend utilise MySQL :

```properties
MYSQL_URL=jdbc:mysql://localhost:3306/meal_planner?createDatabaseIfNotExist=true&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false
MYSQL_USER=root
MYSQL_PASSWORD=root
```

La classe de demarrage Spring Boot est :

```text
backend/src/main/java/com/mealplanner/MealPlannerApplication.java
```

## Securite avant mise en ligne

Pour une application privee, ne pas exposer d'inscription publique est le bon choix. Le plus simple et le plus sur est :

- une page de connexion obligatoire ;
- aucun endpoint d'inscription active par defaut ;
- des utilisateurs crees par toi en base avec des mots de passe hashes ;
- une option de configuration si tu veux activer/desactiver une future inscription ;
- des secrets en variables d'environnement, jamais dans Git.

Avant de deployer, prevoir aussi HTTPS, un mot de passe MySQL fort, un utilisateur MySQL dedie a l'application, et un CORS limite au domaine final.

### Creer le premier utilisateur

Il n'y a pas de formulaire d'inscription public. Pour creer ton premier compte, active temporairement le bootstrap au demarrage :

Avec PowerShell :

```powershell
cd backend
$env:MEAL_PLANNER_BOOTSTRAP_ADMIN_ENABLED="true"
$env:MEAL_PLANNER_ADMIN_USERNAME="guillaume"
$env:MEAL_PLANNER_ADMIN_PASSWORD="un-mot-de-passe-fort"
$env:MEAL_PLANNER_JWT_SECRET="un-secret-long-aleatoire-de-32-caracteres-minimum"
mvn -Pboot-plugin spring-boot:run
```

Avec l'invite de commandes Windows `cmd.exe` :

```bat
cd backend
set MEAL_PLANNER_BOOTSTRAP_ADMIN_ENABLED=true
set MEAL_PLANNER_ADMIN_USERNAME=guillaume
set MEAL_PLANNER_ADMIN_PASSWORD=un-mot-de-passe-fort
set MEAL_PLANNER_JWT_SECRET=un-secret-long-aleatoire-de-32-caracteres-minimum
mvn -Pboot-plugin spring-boot:run
```

Une fois l'utilisateur cree en base, coupe le serveur et relance sans `MEAL_PLANNER_BOOTSTRAP_ADMIN_ENABLED`. Ne mets jamais de mot de passe en clair directement dans la table `app_users` : le backend stocke uniquement un hash PBKDF2.

### Ajouter des utilisateurs ensuite

Le bootstrap sert uniquement a creer le premier admin. Ensuite, connecte-toi avec ce compte et ouvre :

```text
Admin > Utilisateurs
```

Cette page appelle des endpoints proteges par le role `ADMIN` :

```text
GET   /api/admin/users
POST  /api/admin/users
PATCH /api/admin/users/{id}
```

Il n'y a toujours pas d'inscription publique. Une requete SQL directe est possible, mais elle n'est pas recommandee pour l'usage courant parce qu'il faut generer le hash PBKDF2 exact du mot de passe. L'interface admin evite de redemarrer l'application et evite d'ecrire des secrets en clair en base.

Si tu veux relancer en H2 local :

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE="local"
mvn -Pboot-plugin spring-boot:run
```

## Activation Liquibase stricte

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE="liquibase"
mvn -Pliquibase spring-boot:run
```

## Parcours MVP couverts

- Creation, consultation, modification, suppression et recherche de recettes.
- Ingredients dedies et reutilisables, avec categorie de courses.
- Etapes de preparation ordonnees.
- Planning hebdomadaire dejeuner/diner.
- Liste de courses generee depuis le planning, consolidee par ingredient et unite.
- Cases "deja a la maison" et "achete".
- Mode cuisine lisible sur mobile avec navigation entre les etapes.
- Navigation Angular responsive : tableau de bord, recettes, formulaire, planning, courses, mode cuisine.

## Structure API

```text
GET    /api/recipes
POST   /api/recipes
GET    /api/recipes/{id}
PUT    /api/recipes/{id}
DELETE /api/recipes/{id}

GET    /api/ingredients
POST   /api/ingredients

GET    /api/meal-plans/{week}
POST   /api/meal-plans/{week}/items
DELETE /api/meal-plans/{week}/items/{itemId}

GET    /api/shopping-lists/{week}
PATCH  /api/shopping-lists/{week}/items/{itemId}
```

`{week}` accepte une date ISO (`YYYY-MM-DD`) appartenant a la semaine voulue ; l'API normalise automatiquement au lundi.
