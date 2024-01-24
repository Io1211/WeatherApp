INSERT INTO FAVORITE_DATA_CONFIG(ID, SHOW_TEMPERATURE, SHOW_FEELS_LIKE_TEMPERATURE, SHOW_TITLE, SHOW_ICON, SHOW_CLOUDS, SHOW_DEW_POINT, SHOW_HUMIDITY, SHOW_PRESSURE, SHOW_RAIN, SHOW_SNOW, SHOW_SUNRISE, SHOW_SUNSET, SHOW_TIMESTAMP, SHOW_UVI, SHOW_VISIBILITY, SHOW_WIND_DIRECTION, SHOW_WIND_GUST, SHOW_WIND_SPEED) VALUES ('111111', TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE);
INSERT INTO FAVORITE_DATA_CONFIG(ID, SHOW_TEMPERATURE, SHOW_FEELS_LIKE_TEMPERATURE, SHOW_TITLE, SHOW_ICON, SHOW_CLOUDS, SHOW_DEW_POINT, SHOW_HUMIDITY, SHOW_PRESSURE, SHOW_RAIN, SHOW_SNOW, SHOW_SUNRISE, SHOW_SUNSET, SHOW_TIMESTAMP, SHOW_UVI, SHOW_VISIBILITY, SHOW_WIND_DIRECTION, SHOW_WIND_GUST, SHOW_WIND_SPEED) VALUES ('222222', TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE);
INSERT INTO FAVORITE_DATA_CONFIG(ID, SHOW_TEMPERATURE, SHOW_FEELS_LIKE_TEMPERATURE, SHOW_TITLE, SHOW_ICON, SHOW_CLOUDS, SHOW_DEW_POINT, SHOW_HUMIDITY, SHOW_PRESSURE, SHOW_RAIN, SHOW_SNOW, SHOW_SUNRISE, SHOW_SUNSET, SHOW_TIMESTAMP, SHOW_UVI, SHOW_VISIBILITY, SHOW_WIND_DIRECTION, SHOW_WIND_GUST, SHOW_WIND_SPEED) VALUES ('333333', TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE);
INSERT INTO FAVORITE_DATA_CONFIG(ID, SHOW_TEMPERATURE, SHOW_FEELS_LIKE_TEMPERATURE, SHOW_TITLE, SHOW_ICON, SHOW_CLOUDS, SHOW_DEW_POINT, SHOW_HUMIDITY, SHOW_PRESSURE, SHOW_RAIN, SHOW_SNOW, SHOW_SUNRISE, SHOW_SUNSET, SHOW_TIMESTAMP, SHOW_UVI, SHOW_VISIBILITY, SHOW_WIND_DIRECTION, SHOW_WIND_GUST, SHOW_WIND_SPEED) VALUES ('444444', TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE);
INSERT INTO FAVORITE_DATA_CONFIG(ID, SHOW_TEMPERATURE, SHOW_FEELS_LIKE_TEMPERATURE, SHOW_TITLE, SHOW_ICON, SHOW_CLOUDS, SHOW_DEW_POINT, SHOW_HUMIDITY, SHOW_PRESSURE, SHOW_RAIN, SHOW_SNOW, SHOW_SUNRISE, SHOW_SUNSET, SHOW_TIMESTAMP, SHOW_UVI, SHOW_VISIBILITY, SHOW_WIND_DIRECTION, SHOW_WIND_GUST, SHOW_WIND_SPEED) VALUES ('555555', TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE);

INSERT INTO SUBSCRIPTION(ID, SIGNUP_DATE) VALUES (1, DATE '2023-01-01');
INSERT INTO SUBSCRIPTION(ID, SIGNUP_DATE) VALUES (2, DATE '2023-04-04');
INSERT INTO SUBSCRIPTION(ID, SIGNUP_DATE) VALUES (3, DATE '2023-05-05');
INSERT INTO SUBSCRIPTION_PERIOD (ACTIVE, START, STOP, ID) VALUES (FALSE, DATE '2023-01-01', DATE '2023-05-28', 1);
INSERT INTO SUBSCRIPTION_PERIOD (ACTIVE, START, STOP, ID) VALUES (FALSE, DATE '2023-03-19', DATE '2023-06-28', 2);
INSERT INTO SUBSCRIPTION_PERIOD (ACTIVE, START, STOP, ID) VALUES (FALSE, DATE '2023-11-15', DATE '2024-01-15', 3);
INSERT INTO SUBSCRIPTION_PERIOD (ACTIVE, START, STOP, ID) VALUES (TRUE, DATE '2023-05-05', NULL, 4);
INSERT INTO SUBSCRIPTION_SUBSCRIPTION_PERIODS (SUBSCRIPTION_ID, SUBSCRIPTION_PERIODS_ID) VALUES (1, 1);
INSERT INTO SUBSCRIPTION_SUBSCRIPTION_PERIODS (SUBSCRIPTION_ID, SUBSCRIPTION_PERIODS_ID) VALUES (2, 2);
INSERT INTO SUBSCRIPTION_SUBSCRIPTION_PERIODS (SUBSCRIPTION_ID, SUBSCRIPTION_PERIODS_ID) VALUES (2, 3);
INSERT INTO SUBSCRIPTION_SUBSCRIPTION_PERIODS (SUBSCRIPTION_ID, SUBSCRIPTION_PERIODS_ID) VALUES (3, 4);

INSERT INTO USERX (ENABLED, FIRST_NAME, LAST_NAME, PASSWORD, USERNAME, CREATE_USER_USERNAME, CREATE_DATE, FAVORITE_DATA_CONFIG_ID) VALUES (TRUE, 'Admin', 'Istrator', '$2a$10$4ENBx/wSbkRKzj9D5vcnjuif.QpkahXzSNyxi9Ki9Na8O/A/M.HDy', 'admin', 'admin', '2016-01-01 00:00:00', '111111');
INSERT INTO USERX_USERX_ROLE (USERX_USERNAME, ROLES) VALUES ('admin', 'ADMIN');
INSERT INTO USERX_USERX_ROLE (USERX_USERNAME, ROLES) VALUES ('admin', 'REGISTERED_USER');
INSERT INTO USERX (ENABLED, FIRST_NAME, LAST_NAME, PASSWORD, USERNAME, CREATE_USER_USERNAME, CREATE_DATE, FAVORITE_DATA_CONFIG_ID, SUBSCRIPTION_ID) VALUES (TRUE, 'Susi', 'Kaufgern', '$2a$10$4ENBx/wSbkRKzj9D5vcnjuif.QpkahXzSNyxi9Ki9Na8O/A/M.HDy', 'user1', 'admin', '2016-01-01 00:00:00', '222222', 1);
INSERT INTO USERX_USERX_ROLE (USERX_USERNAME, ROLES) VALUES ('user1', 'MANAGER');
INSERT INTO USERX_USERX_ROLE (USERX_USERNAME, ROLES) VALUES ('user1', 'REGISTERED_USER');
INSERT INTO USERX (ENABLED, FIRST_NAME, LAST_NAME, PASSWORD, USERNAME, CREATE_USER_USERNAME, CREATE_DATE, FAVORITE_DATA_CONFIG_ID, SUBSCRIPTION_ID) VALUES (TRUE, 'Max', 'Mustermann', '$2a$10$4ENBx/wSbkRKzj9D5vcnjuif.QpkahXzSNyxi9Ki9Na8O/A/M.HDy', 'user2', 'admin', '2016-01-01 00:00:00', '333333', 2);
INSERT INTO USERX_USERX_ROLE (USERX_USERNAME, ROLES) VALUES ('user2', 'REGISTERED_USER');
INSERT INTO USERX (ENABLED, FIRST_NAME, LAST_NAME, PASSWORD, USERNAME, CREATE_USER_USERNAME, CREATE_DATE, FAVORITE_DATA_CONFIG_ID) VALUES (TRUE, 'Elvis', 'The King', '$2a$10$4ENBx/wSbkRKzj9D5vcnjuif.QpkahXzSNyxi9Ki9Na8O/A/M.HDy', 'elvis', 'elvis', '2016-01-01 00:00:00', '444444');
INSERT INTO USERX_USERX_ROLE (USERX_USERNAME, ROLES) VALUES ('elvis', 'ADMIN');
INSERT INTO USERX_USERX_ROLE (USERX_USERNAME, ROLES) VALUES ('elvis', 'REGISTERED_USER');
INSERT INTO USERX (ENABLED, FIRST_NAME, LAST_NAME, PASSWORD, USERNAME, CREATE_USER_USERNAME, CREATE_DATE, FAVORITE_DATA_CONFIG_ID, SUBSCRIPTION_ID) VALUES (TRUE, 'Prem', 'Mium', '$2a$10$4ENBx/wSbkRKzj9D5vcnjuif.QpkahXzSNyxi9Ki9Na8O/A/M.HDy', 'premium1', 'admin', '2016-01-01 00:00:00', '555555', 3);
INSERT INTO USERX_USERX_ROLE (USERX_USERNAME, ROLES) VALUES ('premium1', 'PREMIUM_USER');
INSERT INTO USERX_USERX_ROLE (USERX_USERNAME, ROLES) VALUES ('premium1', 'REGISTERED_USER');
