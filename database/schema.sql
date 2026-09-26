-- =====================================================================
-- Plaza Órbita - Esquema e Inserciones de Base de Datos MySQL
-- Script completo y corregido: usuarios, negocios, productos, servicios,
-- reseñas.
-- =====================================================================
DROP DATABASE IF EXISTS plaza_orbita;
CREATE DATABASE plaza_orbita CHARACTER SET utf8mb4;
USE plaza_orbita;

-- ---------------------------------------------------------------------
-- 1. ESTRUCTURA DE TABLAS
-- ---------------------------------------------------------------------

-- Usuarios (los 3 roles: ADMIN, BUSINESS_OWNER, CUSTOMER)
CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(120)  NOT NULL,
    email         VARCHAR(150)  NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    role          ENUM('ADMIN','BUSINESS_OWNER','CUSTOMER') NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Negocios dentro de la plaza
CREATE TABLE businesses (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id    BIGINT NOT NULL,
    name        VARCHAR(150) NOT NULL,
    category    ENUM('PRODUCT','SERVICE') NOT NULL,
    subcategory VARCHAR(30) NULL,
    location    VARCHAR(120),
    opens_at    TIME,
    closes_at   TIME,
    phone       VARCHAR(30) NULL,
    status      ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
    image_url   VARCHAR(255) NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Catálogo / inventario (negocios de producto)
CREATE TABLE products (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id   BIGINT NOT NULL,
    name          VARCHAR(150) NOT NULL,
    description   VARCHAR(500),
    price         DECIMAL(10,2) NOT NULL,
    stock         INT NOT NULL DEFAULT 0,
    min_threshold INT NOT NULL DEFAULT 5,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Citas (negocios de servicio)
CREATE TABLE appointments (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id  BIGINT NOT NULL,
    customer_id  BIGINT NOT NULL,
    service_name VARCHAR(150) NOT NULL,
    appt_date    DATE NOT NULL,
    appt_time    TIME NOT NULL,
    status       ENUM('PENDING','CONFIRMED','CANCELLED','COMPLETED') DEFAULT 'PENDING',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uq_no_double_booking (business_id, appt_date, appt_time)
) ENGINE=InnoDB;

-- Pedidos (negocios de producto) + detalle
CREATE TABLE orders (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    status      ENUM('PENDING','READY_FOR_PICKUP','DELIVERED','CANCELLED') DEFAULT 'PENDING',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE order_items (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

-- Notificaciones
CREATE TABLE notifications (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    message    VARCHAR(300) NOT NULL,
    type       ENUM('APPOINTMENT','ORDER','SYSTEM') NOT NULL,
    is_read    BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Servicios (negocios de servicio)
CREATE TABLE services (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id      BIGINT NOT NULL,
    name             VARCHAR(150) NOT NULL,
    description      VARCHAR(300),
    duration_minutes INT NOT NULL DEFAULT 30,
    price            DECIMAL(10,2),
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Reseñas
CREATE TABLE reviews (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    rating      INT NOT NULL,
    comment     VARCHAR(500),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Índices de apoyo para reportes
CREATE INDEX idx_orders_business ON orders(business_id);
CREATE INDEX idx_appt_business ON appointments(business_id);
CREATE INDEX idx_products_business ON products(business_id);

-- ---------------------------------------------------------------------
-- 2. USUARIOS (Se deben insertar antes que los negocios)
-- ---------------------------------------------------------------------
INSERT INTO users (id, name, email, password_hash, role) 
VALUES 
  (1, 'Admin Plaza', 'admin@plazaorbita.com', '$2a$10$HASH_DE_PRUEBA_AQUI', 'ADMIN');


-- ---------------------------------------------------------------------
-- 3. NEGOCIOS 
-- ---------------------------------------------------------------------
INSERT INTO businesses 
  (owner_id, name, category, subcategory, location, opens_at, closes_at, phone, status, image_url)
VALUES
  (1, 'Café Doña Inés',                     'PRODUCT', 'Comida',    'Local 3, Planta Baja',  '08:00:00', '20:00:00', '+52 33 1000 0001', 'ACTIVE', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=500'),
  (1, 'Tacos El Compadre',                  'PRODUCT', 'Comida',    'Local 5, Planta Baja',  '13:00:00', '22:00:00', '+52 33 1000 0002', 'ACTIVE', 'https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=500'),
  (1, 'Estética Lupita',                    'SERVICE', 'Belleza',   'Local 14, Planta Baja', '09:00:00', '19:00:00', '+52 55 1234 5678', 'ACTIVE', 'https://images.unsplash.com/photo-1560066984-138dadb4c035?w=500'),
  (1, 'Barbería Moderna',                   'SERVICE', 'Belleza',   'Local 8, Planta Alta',  '10:00:00', '20:00:00', '+52 33 1000 0004', 'ACTIVE', 'https://images.unsplash.com/photo-1503951914875-452162b0f3f1?w=500'),
  (1, 'Óptica Real',                        'PRODUCT', 'Salud',     'Local 2, Planta Alta',  '09:00:00', '18:00:00', '+52 33 1000 0005', 'ACTIVE', 'https://images.unsplash.com/photo-1591076482161-42ce6da69f67?w=500'),
  (1, 'Farmacia San Rafael',                'PRODUCT', 'Salud',     'Local 1, Planta Baja',  '07:00:00', '23:00:00', '+52 33 1000 0006', 'ACTIVE', 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=500'),
  (1, 'Boutique Luna',                      'PRODUCT', 'Moda',      'Local 10, Planta Alta', '10:00:00', '20:00:00', '+52 33 1000 0007', 'ACTIVE', 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=500'),
  (1, 'Zapatería El Paso',                  'PRODUCT', 'Moda',      'Local 11, Planta Alta', '10:00:00', '20:00:00', '+52 33 1000 0008', 'ACTIVE', 'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=500'),
  (1, 'Tintorería Express',                 'SERVICE', 'Servicios', 'Local 6, Planta Baja',  '08:00:00', '19:00:00', '+52 33 1000 0009', 'ACTIVE', 'https://images.unsplash.com/photo-1517677208171-0bc6725a3e60?w=500'),
  (1, 'TecFix Reparaciones',                'SERVICE', 'Servicios', 'Local 7, Planta Baja',  '09:00:00', '19:00:00', '+52 33 1000 0010', 'ACTIVE', 'https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=500'),
  (1, 'Consultorio Psicológico Vida Plena', 'SERVICE', 'Salud',     'Local 20, Planta Alta', '09:00:00', '18:00:00', '+52 33 1000 0011', 'ACTIVE', 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=500'),
  (1, 'BBVA Sucursal Plaza Órbita',         'SERVICE', 'Servicios', 'Local 1, Planta Alta',  '09:00:00', '16:00:00', '+52 33 1000 0012', 'ACTIVE', 'https://images.unsplash.com/photo-1541354329998-f4d9a9f9297f?w=500');

-- ---------------------------------------------------------------------
-- 4. PRODUCTOS 
-- ---------------------------------------------------------------------
INSERT INTO products (business_id, name, description, price, stock, min_threshold)
SELECT id, 'Café americano', 'Taza de café negro recién hecho', 35.00, 50, 10 FROM businesses WHERE name = 'Café Doña Inés'
UNION ALL SELECT id, 'Café con leche', 'Café con leche entera', 40.00, 45, 10 FROM businesses WHERE name = 'Café Doña Inés'
UNION ALL SELECT id, 'Cappuccino', 'Espuma de leche y canela', 45.00, 30, 8 FROM businesses WHERE name = 'Café Doña Inés'
UNION ALL SELECT id, 'Pan dulce', 'Concha, cuernito o dona', 18.00, 60, 15 FROM businesses WHERE name = 'Café Doña Inés'
UNION ALL SELECT id, 'Sándwich de jamón y queso', 'Pan blanco tostado', 55.00, 20, 5 FROM businesses WHERE name = 'Café Doña Inés'

UNION ALL SELECT id, 'Taco de pastor', 'Con piña, cebolla y cilantro', 15.00, 200, 30 FROM businesses WHERE name = 'Tacos El Compadre'
UNION ALL SELECT id, 'Taco de asada', 'Carne asada con salsa', 18.00, 150, 30 FROM businesses WHERE name = 'Tacos El Compadre'
UNION ALL SELECT id, 'Quesadilla de queso', 'Tortilla de maíz hecha a mano', 30.00, 80, 15 FROM businesses WHERE name = 'Tacos El Compadre'
UNION ALL SELECT id, 'Refresco de lata', '355 ml', 20.00, 100, 20 FROM businesses WHERE name = 'Tacos El Compadre'
UNION ALL SELECT id, 'Agua de horchata', 'Vaso de 500 ml', 25.00, 40, 10 FROM businesses WHERE name = 'Tacos El Compadre'

UNION ALL SELECT id, 'Lentes graduados', 'Armazón básico + mica sencilla', 850.00, 15, 3 FROM businesses WHERE name = 'Óptica Real'
UNION ALL SELECT id, 'Lentes de sol UV400', 'Protección contra rayos UV', 450.00, 25, 5 FROM businesses WHERE name = 'Óptica Real'
UNION ALL SELECT id, 'Líquido para lentes de contacto', 'Frasco 355 ml', 180.00, 30, 8 FROM businesses WHERE name = 'Óptica Real'
UNION ALL SELECT id, 'Estuche para lentes', 'Rígido, varios colores', 90.00, 40, 10 FROM businesses WHERE name = 'Óptica Real'

UNION ALL SELECT id, 'Paracetamol 500mg', 'Caja con 10 tabletas', 35.00, 100, 20 FROM businesses WHERE name = 'Farmacia San Rafael'
UNION ALL SELECT id, 'Vitamina C 1g', 'Frasco con 30 tabletas', 120.00, 60, 15 FROM businesses WHERE name = 'Farmacia San Rafael'
UNION ALL SELECT id, 'Alcohol en gel', '250 ml', 45.00, 80, 20 FROM businesses WHERE name = 'Farmacia San Rafael'
UNION ALL SELECT id, 'Cubrebocas tricapa', 'Paquete con 10 piezas', 25.00, 5, 15 FROM businesses WHERE name = 'Farmacia San Rafael'
UNION ALL SELECT id, 'Suero oral', 'Sobre para rehidratación', 18.00, 70, 20 FROM businesses WHERE name = 'Farmacia San Rafael'

UNION ALL SELECT id, 'Blusa casual', 'Algodón, varias tallas', 280.00, 25, 5 FROM businesses WHERE name = 'Boutique Luna'
UNION ALL SELECT id, 'Vestido de fiesta', 'Corte entallado', 650.00, 8, 3 FROM businesses WHERE name = 'Boutique Luna'
UNION ALL SELECT id, 'Pantalón de mezclilla', 'Corte recto', 420.00, 20, 5 FROM businesses WHERE name = 'Boutique Luna'
UNION ALL SELECT id, 'Chamarra de mezclilla', 'Forro de borrega', 550.00, 2, 5 FROM businesses WHERE name = 'Boutique Luna'

UNION ALL SELECT id, 'Tenis deportivos', 'Varios números', 599.00, 18, 5 FROM businesses WHERE name = 'Zapatería El Paso'
UNION ALL SELECT id, 'Zapato de vestir caballero', 'Piel genuina', 720.00, 12, 4 FROM businesses WHERE name = 'Zapatería El Paso'
UNION ALL SELECT id, 'Sandalias dama', 'Varios colores', 380.00, 22, 6 FROM businesses WHERE name = 'Zapatería El Paso'
UNION ALL SELECT id, 'Botas casuales', 'Piel sintética', 690.00, 3, 5 FROM businesses WHERE name = 'Zapatería El Paso';

-- ---------------------------------------------------------------------
-- 5. SERVICIOS
-- ---------------------------------------------------------------------
INSERT INTO services (business_id, name, description, duration_minutes, price)
SELECT id, 'Corte de cabello', 'Corte moderno o clásico', 30, 150.00 FROM businesses WHERE name = 'Estética Lupita'
UNION ALL SELECT id, 'Manicure', 'Manicure clásico o en gel', 45, 200.00 FROM businesses WHERE name = 'Estética Lupita'
UNION ALL SELECT id, 'Spa facial', 'Limpieza facial profunda', 60, 350.00 FROM businesses WHERE name = 'Estética Lupita'
UNION ALL SELECT id, 'Tinte de cabello', 'Aplicación de tinte y brillo', 90, 450.00 FROM businesses WHERE name = 'Estética Lupita'

UNION ALL SELECT id, 'Corte de cabello caballero', 'Corte clásico o fade', 30, 120.00 FROM businesses WHERE name = 'Barbería Moderna'
UNION ALL SELECT id, 'Arreglo de barba', 'Perfilado y arreglo con navaja', 20, 90.00 FROM businesses WHERE name = 'Barbería Moderna'
UNION ALL SELECT id, 'Corte + barba', 'Paquete completo', 45, 190.00 FROM businesses WHERE name = 'Barbería Moderna'

UNION ALL SELECT id, 'Consulta individual', 'Sesión de 50 minutos', 50, 600.00 FROM businesses WHERE name = 'Consultorio Psicológico Vida Plena'
UNION ALL SELECT id, 'Terapia de pareja', 'Sesión de 60 minutos', 60, 800.00 FROM businesses WHERE name = 'Consultorio Psicológico Vida Plena'
UNION ALL SELECT id, 'Evaluación psicológica', 'Aplicación de pruebas y reporte', 90, 1200.00 FROM businesses WHERE name = 'Consultorio Psicológico Vida Plena'

UNION ALL SELECT id, 'Apertura de cuenta', 'Asesoría para nueva cuenta', 30, NULL FROM businesses WHERE name = 'BBVA Sucursal Plaza Órbita'
UNION ALL SELECT id, 'Asesoría de crédito', 'Información sobre créditos y tarjetas', 30, NULL FROM businesses WHERE name = 'BBVA Sucursal Plaza Órbita'
UNION ALL SELECT id, 'Aclaración de movimiento', 'Revisión de cargos o depósitos', 20, NULL FROM businesses WHERE name = 'BBVA Sucursal Plaza Órbita'

UNION ALL SELECT id, 'Lavado de traje', 'Lavado en seco', 1440, 180.00 FROM businesses WHERE name = 'Tintorería Express'
UNION ALL SELECT id, 'Lavado de vestido', 'Lavado en seco para vestidos', 1440, 250.00 FROM businesses WHERE name = 'Tintorería Express'
UNION ALL SELECT id, 'Planchado urgente', 'Entrega en 2 horas', 120, 60.00 FROM businesses WHERE name = 'Tintorería Express'

UNION ALL SELECT id, 'Cambio de pantalla', 'Reemplazo de pantalla de celular', 60, 900.00 FROM businesses WHERE name = 'TecFix Reparaciones'
UNION ALL SELECT id, 'Cambio de batería', 'Reemplazo de batería', 40, 450.00 FROM businesses WHERE name = 'TecFix Reparaciones'
UNION ALL SELECT id, 'Diagnóstico general', 'Revisión completa del equipo', 20, 100.00 FROM businesses WHERE name = 'TecFix Reparaciones';

-- ---------------------------------------------------------------------
-- 6. RESEÑAS 
-- ---------------------------------------------------------------------
INSERT INTO reviews (business_id, customer_id, rating, comment, created_at)
SELECT b.id, u.id, r.rating, r.comment, NOW() - INTERVAL FLOOR(RAND()*30) DAY
FROM businesses b
CROSS JOIN (SELECT id FROM users WHERE role = 'CUSTOMER' LIMIT 1) u
CROSS JOIN (
    SELECT 5 AS rating, 'Excelente atención, totalmente recomendado.' AS comment UNION ALL
    SELECT 4, 'Muy buen servicio, aunque tardaron un poco.' UNION ALL
    SELECT 5, 'Superó mis expectativas, volveré pronto.' UNION ALL
    SELECT 3, 'Estuvo bien, nada extraordinario.' UNION ALL
    SELECT 5, 'Trato muy amable y buen precio.'
) r;