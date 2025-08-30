CREATE TABLE ingredients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    unit VARCHAR(50),
    recipe_id BIGINT NOT NULL,
    CONSTRAINT fk_ingredients_recipe FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);