-- =========================================
-- Création de 10 recettes
-- =========================================
INSERT INTO public.recipes (difficulty, author_id, cooking_time, created_at, category, description, image_url, instructions, title)
VALUES
(2, 1, 30, NOW(), 'DESSERT', 'Délicieux gâteau au chocolat facile à réaliser.', 'https://picsum.photos/200/150?random=1', '1. Préchauffer le four à 180°C. 2. Mélanger les ingrédients...', 'Gâteau au chocolat'),
(3, 1, 45, NOW(), 'MAIN_COURSE', 'Poulet rôti aux herbes, simple et savoureux.', 'https://picsum.photos/200/150?random=2', '1. Préchauffer le four à 200°C. 2. Assaisonner le poulet...', 'Poulet rôti aux herbes'),
(1, 1, 15, NOW(), 'STARTER', 'Salade fraîche avec vinaigrette maison.', 'https://picsum.photos/200/150?random=3', '1. Couper les légumes. 2. Préparer la vinaigrette...', 'Salade fraîcheur'),
(4, 1, 60, NOW(), 'MAIN_COURSE', 'Lasagnes maison riches en saveurs.', 'https://picsum.photos/200/150?random=4', '1. Préparer la sauce. 2. Monter les lasagnes...', 'Lasagnes maison'),
(2, 1, 25, NOW(), 'DESSERT', 'Tarte aux pommes croustillante et sucrée.', 'https://picsum.photos/200/150?random=5', '1. Étaler la pâte. 2. Ajouter les pommes...', 'Tarte aux pommes'),
(3, 1, 40, NOW(), 'MAIN_COURSE', 'Curry de légumes épicé et savoureux.', 'https://picsum.photos/200/150?random=6', '1. Faire revenir les légumes. 2. Ajouter les épices...', 'Curry de légumes'),
(2, 1, 20, NOW(), 'DESSERT', 'Mousse au chocolat légère.', 'https://picsum.photos/200/150?random=7', '1. Faire fondre le chocolat. 2. Monter les blancs en neige...', 'Mousse au chocolat'),
(3, 1, 35, NOW(), 'MAIN_COURSE', 'Quiche aux légumes de saison.', 'https://picsum.photos/200/150?random=8', '1. Préparer la pâte. 2. Garnir avec légumes...', 'Quiche aux légumes'),
(1, 1, 10, NOW(), 'DRINK', 'Smoothie fruité vitaminé.', 'https://picsum.photos/200/150?random=9', '1. Mettre tous les fruits dans le blender. 2. Mixer...', 'Smoothie vitaminé'),
(4, 1, 50, NOW(), 'MAIN_COURSE', 'Boeuf bourguignon traditionnel.', 'https://picsum.photos/200/150?random=10', '1. Faire revenir le boeuf. 2. Ajouter le vin et les légumes...', 'Boeuf bourguignon');