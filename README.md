# Gourmich

Gourmich est une application web de gestion et partage de recettes culinaires. Elle permet aux utilisateurs de créer, lire, mettre à jour et supprimer des recettes (CRUD), de gérer leur compte via authentification et de gérer leurs favoris.  

Le projet est développé avec **Angular 19** pour le front-end et **Spring Boot / Java** pour le back-end, et est **déployé sur Render** pour un accès immédiat en ligne.

---

## Badges

![Angular](https://img.shields.io/badge/Angular-19-red)
![Node.js](https://img.shields.io/badge/Node.js-20-green)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

---

## Table des matières

- [Fonctionnalités](#fonctionnalités)  
- [Technologies utilisées](#technologies-utilisées)  
- [Accéder à l'application](#accéder-à-lapplication)    
- [Prochaines améliorations](#prochaines-améliorations)  
- [Licence](#licence)  

---

## Fonctionnalités

- Authentification utilisateur via login/mot de passe
- Gestion des recettes : création, lecture, mise à jour, suppression (CRUD)
- Gestion des favoris : ajout et suppression de recettes favorites
- Refonte UI avec composants **PrimeNG**
- Responsive partiel (desktop complet, menu mobile/tablette à implémenter)
- CI/CD avec GitHub Actions et déploiement automatique sur [Render](https://render.com/)

---

## Technologies utilisées

- **Front-end** : Angular 19, TypeScript, Tailwind CSS, PrimeNG  
- **Back-end** : Spring Boot, Java, PostgreSQL  
- **Outils & CI/CD** : GitHub Actions, Render, Docker  
- **Tests** : JUnit 5, Spring Boot Test, Mockito

---

## Accéder à l'application

L'application est déployée et disponible en ligne :  

[**Ouvrir Gourmich sur Render**](https://gourmichv2.onrender.com)

---

## Prochaines améliorations (V2)

- Implémenter le menu mobile/tablette
- Finaliser la refonte de la palette de couleurs et des typographies  
- Ajouter des tests front-end unitaires et e2e  
- Ajouter des filtres et une barre de recherche pour les recettes  
- Intégrer de l’IA pour suggérer des recettes automatiquement  

---

## Déploiement

- Déploiement front-end et back-end sur [Render](https://render.com)  
- CI/CD via GitHub Actions avec build & déploiement automatique  

---

## Licence

Ce projet est sous licence MIT – voir le fichier [LICENSE](LICENSE) pour plus de détails.
