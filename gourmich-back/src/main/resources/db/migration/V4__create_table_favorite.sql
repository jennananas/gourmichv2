CREATE TABLE favorite (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    add_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_favorite_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_recipe FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    CONSTRAINT uc_favorite UNIQUE(user_id, recipe_id)
);