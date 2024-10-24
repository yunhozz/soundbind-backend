DROP TABLE IF EXISTS orchestration_process_step;
DROP TABLE IF EXISTS orchestration_process;

CREATE TABLE orchestration_process (
     id VARCHAR(36) NOT NULL PRIMARY KEY,
     status VARCHAR(50)
);

CREATE TABLE orchestration_process_step (
     id VARCHAR(36) NOT NULL PRIMARY KEY,
     orchestration_process_id VARCHAR(36),
     name VARCHAR(100),
     step_status VARCHAR(50),
     step_type VARCHAR(50),
     error VARCHAR(10000),
     FOREIGN KEY (orchestration_process_id) REFERENCES orchestration_process(id)
);