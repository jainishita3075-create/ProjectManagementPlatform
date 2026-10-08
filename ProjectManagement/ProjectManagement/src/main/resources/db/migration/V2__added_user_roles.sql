CREATE TABLE user_roles (
    role_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles(role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    PRIMARY KEY (user_id, role_id)
);