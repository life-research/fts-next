DROP SCHEMA IF EXISTS `gics` ;
CREATE SCHEMA IF NOT EXISTS `gics` DEFAULT CHARACTER SET utf8 collate utf8_bin;

USE gics;

CREATE TABLE consent
(
    PATIENTSIGNATURE_IS_FROM_GUARDIAN bit DEFAULT 0,
    PHYSICIANID varchar(255),
    CONSENT_DATE timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    VIRTUAL_PERSON_ID bigint NOT NULL,
    CT_DOMAIN_NAME varchar(50) NOT NULL,
    CT_NAME varchar(100) NOT NULL,
    CT_VERSION int NOT NULL,
    COMMENT varchar(255),
    EXTERN_PROPERTIES varchar(4095),
    EXPIRATION_PROPERTIES varchar(255),
    CREATE_TIMESTAMP timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UPDATE_TIMESTAMP timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    VALID_FROM datetime(3),
    FHIR_ID VARCHAR(41) NOT NULL DEFAULT '',
    CONSTRAINT C_PRIMARY PRIMARY KEY (CONSENT_DATE,VIRTUAL_PERSON_ID,CT_DOMAIN_NAME,CT_NAME,CT_VERSION)
) collate utf8_bin
;
CREATE TABLE consent_template
(
    TITLE varchar(255),
    COMMENT varchar(255),
    EXTERN_PROPERTIES varchar(4095),
    EXPIRATION_PROPERTIES varchar(255),
    NAME varchar(100) NOT NULL,
    VERSION int NOT NULL,
    DOMAIN_NAME varchar(50) NOT NULL,
    `TYPE` varchar(20) NOT NULL,
    FOOTER varchar(255),
    HEADER varchar(255),
    SCAN varchar(255),
    CREATE_TIMESTAMP timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UPDATE_TIMESTAMP timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    LABEL varchar(255),
    VERSION_LABEL varchar(255),
    FINALISED TINYINT(1),
    FHIR_ID VARCHAR(41) NOT NULL DEFAULT '',
    VALID_FROM_PROPERTIES varchar(255),
    CONSTRAINT C_PRIMARY PRIMARY KEY (NAME,VERSION,DOMAIN_NAME)
) collate utf8_bin
;
CREATE TABLE mapped_consent_template
(
    FROM_NAME varchar(100) NOT NULL,
    FROM_DOMAIN_NAME varchar(50) NOT NULL,
    FROM_VERSION int NOT NULL,
    TO_NAME varchar(100) NOT NULL,
    TO_DOMAIN_NAME varchar(50) NOT NULL,
    TO_VERSION int NOT NULL,
    CONSTRAINT C_PRIMARY PRIMARY KEY (FROM_NAME,FROM_DOMAIN_NAME,FROM_VERSION,TO_NAME,TO_DOMAIN_NAME,TO_VERSION)
) collate utf8_bin
;
CREATE TABLE domain
(
    NAME varchar(50) PRIMARY KEY NOT NULL,
    COMMENT varchar(255),
    CT_VERSION_CONVERTER varchar(255),
    EXTERN_PROPERTIES varchar(4095),
    EXPIRATION_PROPERTIES varchar(255),
    LABEL varchar(255),
    MODULE_VERSION_CONVERTER varchar(255),
    POLICY_VERSION_CONVERTER varchar(255),
    CONFIG text NOT NULL,
    LOGO longtext,
    FINALISED TINYINT(1),
    CREATE_TIMESTAMP timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UPDATE_TIMESTAMP timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    FHIR_ID VARCHAR(41) NOT NULL DEFAULT ''
) collate utf8_bin
;
CREATE TABLE free_text_def
(
    COMMENT varchar(255),
    CONVERTERSTRING varchar(255),
    POS int,
    REQUIRED bit DEFAULT 0,
    TYPE int,
    FREETEXT_NAME varchar(255) NOT NULL,
    DOMAIN_NAME varchar(50) NOT NULL,
    CT_NAME varchar(100) NOT NULL,
    CT_VERSION int NOT NULL,
    EXTERN_PROPERTIES varchar(4095),
    LABEL varchar(255),
    FINALISED TINYINT(1),
    CREATE_TIMESTAMP timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UPDATE_TIMESTAMP timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    FHIR_ID VARCHAR(41) NOT NULL DEFAULT '',
    CONSTRAINT C_PRIMARY PRIMARY KEY (FREETEXT_NAME,DOMAIN_NAME,CT_NAME,CT_VERSION)
) collate utf8_bin
;
CREATE TABLE free_text_val
(
    VALUE longtext,
    FREETEXTDEV_NAME varchar(255) NOT NULL,
    CONSENT_DATE timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSENT_VIRTUAL_PERSON_ID bigint NOT NULL,
    CT_DOMAIN_NAME varchar(50) NOT NULL,
    CT_NAME varchar(100) NOT NULL,
    CT_VERSION int NOT NULL,
    FHIR_ID VARCHAR(41) NOT NULL DEFAULT '',
    CONSTRAINT C_PRIMARY PRIMARY KEY (FREETEXTDEV_NAME,CONSENT_DATE,CONSENT_VIRTUAL_PERSON_ID,CT_DOMAIN_NAME,CT_NAME,CT_VERSION)
) collate utf8_bin
;
CREATE TABLE module
(
    COMMENT varchar(255),
    EXTERN_PROPERTIES varchar(4095),
    TITLE varchar(255),
    NAME varchar(100) NOT NULL,
    VERSION int NOT NULL,
    DOMAIN_NAME varchar(50) NOT NULL,
    TEXT varchar(255),
    SHORT_TEXT varchar(5000) NULL,
    CREATE_TIMESTAMP timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UPDATE_TIMESTAMP timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    LABEL varchar(255),
    FINALISED TINYINT(1),
    FHIR_ID VARCHAR(41) NOT NULL DEFAULT '',
    CONSTRAINT C_PRIMARY PRIMARY KEY (NAME,VERSION,DOMAIN_NAME)
) collate utf8_bin
;
CREATE TABLE module_consent_template
(
    COMMENT varchar(255),
    DEFAULTCONSENTSTATUS int,
    DISPLAYCHECKBOXES bigint,
    EXTERN_PROPERTIES varchar(4095),
    MANDATORY bit DEFAULT 0,
    ORDER_NUMBER int,
    CT_DOMAIN varchar(50) NOT NULL,
    CT_NAME varchar(100) NOT NULL,
    CT_VERSION int NOT NULL,
    M_DOMAIN varchar(50) NOT NULL,
    M_NAME varchar(100) NOT NULL,
    M_VERSION int NOT NULL,
    PARENT_M_DOMAIN varchar(50),
    PARENT_M_NAME varchar(100),
    PARENT_M_VERSION int,
    EXPIRATION_PROPERTIES varchar(255),
    FHIR_ID VARCHAR(41) NOT NULL DEFAULT '',
    CONSTRAINT C_PRIMARY PRIMARY KEY (CT_DOMAIN,CT_NAME,CT_VERSION,M_DOMAIN,M_NAME,M_VERSION)
) collate utf8_bin
;
CREATE TABLE module_policy
(
    P_NAME varchar(100) NOT NULL,
    P_DOMAIN_NAME varchar(50) NOT NULL,
    P_VERSION int NOT NULL,
    M_NAME varchar(100) NOT NULL,
    M_DOMAIN_NAME varchar(50) NOT NULL,
    M_VERSION int NOT NULL,
    COMMENT varchar(255),
    EXTERN_PROPERTIES varchar(4095),
    EXPIRATION_PROPERTIES varchar(255),
    FHIR_ID VARCHAR(41) NOT NULL DEFAULT '',
    CONSTRAINT C_PRIMARY PRIMARY KEY (P_NAME,P_DOMAIN_NAME,P_VERSION,M_NAME,M_DOMAIN_NAME,M_VERSION)
) collate utf8_bin
;
CREATE TABLE policy
(
    COMMENT varchar(255),
    EXTERN_PROPERTIES varchar(255),
    NAME varchar(100) NOT NULL,
    VERSION int NOT NULL,
    DOMAIN_NAME varchar(50) NOT NULL,
    CREATE_TIMESTAMP timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UPDATE_TIMESTAMP timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    LABEL varchar(255),
    FINALISED TINYINT(1),
    FHIR_ID VARCHAR(41) NOT NULL DEFAULT '',
    CONSTRAINT C_PRIMARY PRIMARY KEY (NAME,VERSION,DOMAIN_NAME)
) collate utf8_bin
;
CREATE TABLE sequence
(
    SEQ_NAME varchar(50) PRIMARY KEY NOT NULL,
    SEQ_COUNT decimal(38,0)
) collate utf8_bin
;
CREATE TABLE signature
(
    SIGNATUREDATE timestamp(3),
    SIGNATUREPLACE varchar(255),
    SIGNATURESCANBASE64 longtext,
    TYPE int NOT NULL,
    CONSENT_DATE timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSENT_VIRTUAL_PERSON_ID bigint NOT NULL,
    CT_DOMAIN_NAME varchar(50) NOT NULL,
    CT_NAME varchar(100) NOT NULL,
    CT_VERSION int NOT NULL,
    FHIR_ID VARCHAR(41) NOT NULL DEFAULT '',
    CONSTRAINT C_PRIMARY PRIMARY KEY (TYPE,CONSENT_DATE,CONSENT_VIRTUAL_PERSON_ID,CT_DOMAIN_NAME,CT_NAME,CT_VERSION)
) collate utf8_bin
;
CREATE TABLE signed_policy
(
    STATUS int,
    CONSENT_DATE timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSENT_VIRTUAL_PERSON_ID bigint NOT NULL,
    CT_DOMAIN_NAME varchar(50) NOT NULL,
    CT_NAME varchar(100) NOT NULL,
    CT_VERSION int NOT NULL,
    POLICY_DOMAIN_NAME varchar(50) NOT NULL,
    POLICY_NAME varchar(100) NOT NULL,
    POLICY_VERSION int NOT NULL,
    FHIR_ID VARCHAR(41) NOT NULL DEFAULT '',
    CONSTRAINT C_PRIMARY PRIMARY KEY (CONSENT_DATE,CONSENT_VIRTUAL_PERSON_ID,CT_DOMAIN_NAME,CT_NAME,CT_VERSION,POLICY_DOMAIN_NAME,POLICY_NAME,POLICY_VERSION)
) collate utf8_bin
;
CREATE TABLE signer_id
(
    VALUE varchar(255) NOT NULL,
    SIT_DOMAIN_NAME varchar(50) NOT NULL,
    SIT_NAME varchar(100) NOT NULL,
    CREATE_TIMESTAMP timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    FHIR_ID VARCHAR(41) NOT NULL DEFAULT '',
    CONSTRAINT C_PRIMARY PRIMARY KEY (VALUE,SIT_DOMAIN_NAME,SIT_NAME)
) collate utf8_bin
;
CREATE TABLE signer_id_type
(
    NAME varchar(100) NOT NULL,
    DOMAIN_NAME varchar(50) NOT NULL,
    CREATE_TIMESTAMP timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UPDATE_TIMESTAMP timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    LABEL varchar(255),
    COMMENT varchar(255),
    ORDER_NUMBER int NOT NULL DEFAULT 0,
    FHIR_ID VARCHAR(41) NOT NULL DEFAULT '',
    CONSTRAINT C_PRIMARY PRIMARY KEY (NAME,DOMAIN_NAME)
) collate utf8_bin
;
CREATE TABLE text
(
    ID varchar(255) PRIMARY KEY NOT NULL,
    TEXT longtext
) collate utf8_bin
;
CREATE TABLE consent_scan
(
    SCANBASE64 longtext,
    FILETYPE varchar(255),
    FHIR_ID VARCHAR(41) NOT NULL PRIMARY KEY DEFAULT '',
    CONSENT_DATE timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    VIRTUAL_PERSON_ID bigint NOT NULL,
    CT_DOMAIN_NAME varchar(50) NOT NULL,
    CT_NAME varchar(100) NOT NULL,
    CT_VERSION int NOT NULL,
    FILENAME varchar(255) NOT NULL DEFAULT '',
    UPLOAD_DATE timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) collate utf8_bin
;
CREATE TABLE consent_template_scan
(
    ID varchar(255) PRIMARY KEY NOT NULL,
    SCANBASE64 longtext,
    FILETYPE varchar(255)
) collate utf8_bin
;


CREATE TABLE virtual_person
(
    ID bigint PRIMARY KEY NOT NULL,
    CREATE_TIMESTAMP timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) collate utf8_bin
;
CREATE TABLE virtual_person_signer_id
(
    SIT_NAME varchar(100) NOT NULL,
    SIT_DOMAIN_NAME varchar(50) NOT NULL,
    SI_VALUE varchar(255) NOT NULL,
    VP_ID bigint NOT NULL,
    CONSTRAINT C_PRIMARY PRIMARY KEY (SIT_NAME,SIT_DOMAIN_NAME,SI_VALUE,VP_ID)
) collate utf8_bin
;
ALTER TABLE consent
    ADD CONSTRAINT FK_consent_CT_NAME
        FOREIGN KEY
            (
             CT_NAME,
             CT_VERSION,
             CT_DOMAIN_NAME
                )
            REFERENCES consent_template
                (
                 NAME,
                 VERSION,
                 DOMAIN_NAME
                    )
;
ALTER TABLE consent
    ADD CONSTRAINT FK_consent_VIRTUAL_PERSON_ID
        FOREIGN KEY (VIRTUAL_PERSON_ID)
            REFERENCES virtual_person(ID)
;

ALTER TABLE consent_scan
    ADD CONSTRAINT FK_scan_CONSENT
        FOREIGN KEY
            (
             CONSENT_DATE,
             VIRTUAL_PERSON_ID,
             CT_DOMAIN_NAME,
             CT_NAME,
             CT_VERSION
                )
            REFERENCES consent
                (
                 CONSENT_DATE,
                 VIRTUAL_PERSON_ID,
                 CT_DOMAIN_NAME,
                 CT_NAME,
                 CT_VERSION
                    )
;

CREATE INDEX I_FK_consent_CT_NAME ON consent
    (
     CT_NAME,
     CT_VERSION,
     CT_DOMAIN_NAME
        )
;
CREATE UNIQUE INDEX I_PRIMARY ON consent
    (
     CONSENT_DATE,
     VIRTUAL_PERSON_ID,
     CT_DOMAIN_NAME,
     CT_NAME,
     CT_VERSION
        )
;
CREATE INDEX I_FK_consent_VIRTUAL_PERSON_ID ON consent(VIRTUAL_PERSON_ID)
;
ALTER TABLE consent_template
    ADD CONSTRAINT FK_consent_template_FOOTER
        FOREIGN KEY (FOOTER)
            REFERENCES text(ID)
;
ALTER TABLE consent_template
    ADD CONSTRAINT FK_consent_template_DOMAIN_NAME
        FOREIGN KEY (DOMAIN_NAME)
            REFERENCES domain(NAME)
;
ALTER TABLE consent_template
    ADD CONSTRAINT FK_consent_template_SCAN_BASE64
        FOREIGN KEY (SCAN)
            REFERENCES consent_template_scan(ID)
;
ALTER TABLE consent_template
    ADD CONSTRAINT FK_consent_template_HEADER
        FOREIGN KEY (HEADER)
            REFERENCES text(ID)
;
ALTER TABLE consent_template
    ADD CONSTRAINT FK_consent_template_TITLE
        FOREIGN KEY (TITLE)
            REFERENCES text(ID)
;
CREATE UNIQUE INDEX I_PRIMARY ON consent_template
    (
     NAME,
     VERSION,
     DOMAIN_NAME
        )
;
CREATE INDEX I_FK_consent_template_TITLE ON consent_template(TITLE)
;
CREATE INDEX I_FK_module_TITLE ON consent_template(TITLE)
;
CREATE INDEX I_FK_consent_template_HEADER ON consent_template(HEADER)
;
CREATE INDEX I_FK_consent_template_FOOTER ON consent_template(FOOTER)
;
CREATE INDEX I_FK_consent_template_DOMAIN_NAME ON consent_template(DOMAIN_NAME)
;
CREATE UNIQUE INDEX I_PRIMARY ON domain(NAME)
;
ALTER TABLE free_text_def
    ADD CONSTRAINT FK_free_text_def_CT_NAME
        FOREIGN KEY
            (
             CT_NAME,
             CT_VERSION,
             DOMAIN_NAME
                )
            REFERENCES consent_template
                (
                 NAME,
                 VERSION,
                 DOMAIN_NAME
                    )
;
CREATE UNIQUE INDEX I_PRIMARY ON free_text_def
    (
     FREETEXT_NAME,
     DOMAIN_NAME,
     CT_NAME,
     CT_VERSION
        )
;
CREATE INDEX I_FK_free_text_def_CT_NAME ON free_text_def
    (
     CT_NAME,
     CT_VERSION,
     DOMAIN_NAME
        )
;
ALTER TABLE free_text_val
    ADD CONSTRAINT FK_free_text_val_CONSENT_DATE
        FOREIGN KEY
            (
             CONSENT_DATE,
             CONSENT_VIRTUAL_PERSON_ID,
             CT_DOMAIN_NAME,
             CT_NAME,
             CT_VERSION
                )
            REFERENCES consent
                (
                 CONSENT_DATE,
                 VIRTUAL_PERSON_ID,
                 CT_DOMAIN_NAME,
                 CT_NAME,
                 CT_VERSION
                    )
;
CREATE UNIQUE INDEX I_PRIMARY ON free_text_val
    (
     FREETEXTDEV_NAME,
     CONSENT_DATE,
     CONSENT_VIRTUAL_PERSON_ID,
     CT_DOMAIN_NAME,
     CT_NAME,
     CT_VERSION
        )
;
CREATE INDEX I_FK_free_text_val_CONSENT_DATE ON free_text_val
    (
     CONSENT_DATE,
     CONSENT_VIRTUAL_PERSON_ID,
     CT_DOMAIN_NAME,
     CT_NAME,
     CT_VERSION
        )
;
ALTER TABLE module
    ADD CONSTRAINT FK_module_TEXT
        FOREIGN KEY (TEXT)
            REFERENCES text(ID)
;
ALTER TABLE module
    ADD CONSTRAINT FK_module_TITLE
        FOREIGN KEY (TITLE)
            REFERENCES text(ID)
;
ALTER TABLE module
    ADD CONSTRAINT FK_module_DOMAIN_NAME
        FOREIGN KEY (DOMAIN_NAME)
            REFERENCES domain(NAME)
;
CREATE UNIQUE INDEX I_PRIMARY ON module
    (
     NAME,
     VERSION,
     DOMAIN_NAME
        )
;
CREATE INDEX I_FK_module_TEXT ON module(TEXT)
;
CREATE INDEX I_FK_module_DOMAIN_NAME ON module(DOMAIN_NAME)
;
ALTER TABLE module_consent_template
    ADD CONSTRAINT FK_module_consent_template_CT_NAME
        FOREIGN KEY
            (
             CT_NAME,
             CT_VERSION,
             CT_DOMAIN
                )
            REFERENCES consent_template
                (
                 NAME,
                 VERSION,
                 DOMAIN_NAME
                    )
;
ALTER TABLE module_consent_template
    ADD CONSTRAINT FK_module_consent_template_M_NAME
        FOREIGN KEY
            (
             M_NAME,
             M_VERSION,
             M_DOMAIN
                )
            REFERENCES module
                (
                 NAME,
                 VERSION,
                 DOMAIN_NAME
                    )
;
ALTER TABLE module_consent_template
    ADD CONSTRAINT FK_module_consent_template_PARENT_M_NAME
        FOREIGN KEY
            (
             PARENT_M_NAME,
             PARENT_M_VERSION,
             PARENT_M_DOMAIN
                )
            REFERENCES module
                (
                 NAME,
                 VERSION,
                 DOMAIN_NAME
                    )
;
CREATE INDEX I_FK_module_consent_template_PARENT_M_NAME ON module_consent_template
    (
     PARENT_M_NAME,
     PARENT_M_VERSION,
     PARENT_M_DOMAIN
        )
;
CREATE INDEX I_FK_module_consent_template_CT_NAME ON module_consent_template
    (
     CT_NAME,
     CT_VERSION,
     CT_DOMAIN
        )
;
CREATE UNIQUE INDEX I_PRIMARY ON module_consent_template
    (
     CT_DOMAIN,
     CT_NAME,
     CT_VERSION,
     M_DOMAIN,
     M_NAME,
     M_VERSION
        )
;
CREATE INDEX I_FK_module_consent_template_M_NAME ON module_consent_template
    (
     M_NAME,
     M_VERSION,
     M_DOMAIN
        )
;
ALTER TABLE module_policy
    ADD CONSTRAINT FK_MODULE_POLICY_M_NAME
        FOREIGN KEY
            (
             M_NAME,
             M_VERSION,
             M_DOMAIN_NAME
                )
            REFERENCES module
                (
                 NAME,
                 VERSION,
                 DOMAIN_NAME
                    )
;
ALTER TABLE module_policy
    ADD CONSTRAINT FK_MODULE_POLICY_P_NAME
        FOREIGN KEY
            (
             P_NAME,
             P_VERSION,
             P_DOMAIN_NAME
                )
            REFERENCES policy
                (
                 NAME,
                 VERSION,
                 DOMAIN_NAME
                    )
;
CREATE INDEX I_FK_MODULE_POLICY_M_NAME ON module_policy
    (
     M_NAME,
     M_VERSION,
     M_DOMAIN_NAME
        )
;
CREATE UNIQUE INDEX I_PRIMARY ON module_policy
    (
     P_NAME,
     P_DOMAIN_NAME,
     P_VERSION,
     M_NAME,
     M_DOMAIN_NAME,
     M_VERSION
        )
;
CREATE INDEX I_FK_MODULE_POLICY_P_NAME ON module_policy
    (
     P_NAME,
     P_VERSION,
     P_DOMAIN_NAME
        )
;
ALTER TABLE policy
    ADD CONSTRAINT FK_policy_DOMAIN_NAME
        FOREIGN KEY (DOMAIN_NAME)
            REFERENCES domain(NAME)
;
CREATE UNIQUE INDEX I_PRIMARY ON policy
    (
     NAME,
     VERSION,
     DOMAIN_NAME
        )
;
CREATE INDEX I_FK_policy_DOMAIN_NAME ON policy(DOMAIN_NAME)
;
CREATE UNIQUE INDEX I_PRIMARY ON sequence(SEQ_NAME)
;
ALTER TABLE signature
    ADD CONSTRAINT FK_signature_CONSENT_DATE
        FOREIGN KEY
            (
             CONSENT_DATE,
             CONSENT_VIRTUAL_PERSON_ID,
             CT_DOMAIN_NAME,
             CT_NAME,
             CT_VERSION
                )
            REFERENCES consent
                (
                 CONSENT_DATE,
                 VIRTUAL_PERSON_ID,
                 CT_DOMAIN_NAME,
                 CT_NAME,
                 CT_VERSION
                    )
;
CREATE UNIQUE INDEX I_PRIMARY ON signature
    (
     TYPE,
     CONSENT_DATE,
     CONSENT_VIRTUAL_PERSON_ID,
     CT_DOMAIN_NAME,
     CT_NAME,
     CT_VERSION
        )
;
CREATE INDEX I_FK_signature_CONSENT_DATE ON signature
    (
     CONSENT_DATE,
     CONSENT_VIRTUAL_PERSON_ID,
     CT_DOMAIN_NAME,
     CT_NAME,
     CT_VERSION
        )
;
ALTER TABLE signed_policy
    ADD CONSTRAINT FK_signed_policy_POLICY_NAME
        FOREIGN KEY
            (
             POLICY_NAME,
             POLICY_VERSION,
             POLICY_DOMAIN_NAME
                )
            REFERENCES policy
                (
                 NAME,
                 VERSION,
                 DOMAIN_NAME
                    )
;
ALTER TABLE signed_policy
    ADD CONSTRAINT FK_signed_policy_CONSENT_DATE
        FOREIGN KEY
            (
             CONSENT_DATE,
             CONSENT_VIRTUAL_PERSON_ID,
             CT_DOMAIN_NAME,
             CT_NAME,
             CT_VERSION
                )
            REFERENCES consent
                (
                 CONSENT_DATE,
                 VIRTUAL_PERSON_ID,
                 CT_DOMAIN_NAME,
                 CT_NAME,
                 CT_VERSION
                    )
;
CREATE UNIQUE INDEX I_PRIMARY ON signed_policy
    (
     CONSENT_DATE,
     CONSENT_VIRTUAL_PERSON_ID,
     CT_DOMAIN_NAME,
     CT_NAME,
     CT_VERSION,
     POLICY_DOMAIN_NAME,
     POLICY_NAME,
     POLICY_VERSION
        )
;
CREATE INDEX I_FK_signed_policy_POLICY_NAME ON signed_policy
    (
     POLICY_NAME,
     POLICY_VERSION,
     POLICY_DOMAIN_NAME
        )
;
ALTER TABLE signer_id
    ADD CONSTRAINT FK_signer_id_SIT_NAME
        FOREIGN KEY
            (
             SIT_NAME,
             SIT_DOMAIN_NAME
                )
            REFERENCES signer_id_type
                (
                 NAME,
                 DOMAIN_NAME
                    )
;
CREATE UNIQUE INDEX I_PRIMARY ON signer_id
    (
     VALUE,
     SIT_DOMAIN_NAME,
     SIT_NAME
        )
;
CREATE INDEX I_FK_signer_id_SIT_NAME ON signer_id
    (
     SIT_NAME,
     SIT_DOMAIN_NAME
        )
;
ALTER TABLE signer_id_type
    ADD CONSTRAINT FK_signer_id_type_DOMAIN_NAME
        FOREIGN KEY (DOMAIN_NAME)
            REFERENCES domain(NAME)
;
CREATE INDEX I_FK_signer_id_type_DOMAIN_NAME ON signer_id_type(DOMAIN_NAME)
;
CREATE UNIQUE INDEX I_PRIMARY ON signer_id_type
    (
     NAME,
     DOMAIN_NAME
        )
;
CREATE UNIQUE INDEX I_PRIMARY ON text(ID)
;
CREATE UNIQUE INDEX I_PRIMARY ON virtual_person(ID)
;
ALTER TABLE virtual_person_signer_id
    ADD CONSTRAINT FK_VIRTUAL_PERSON_SIGNER_ID_VP_ID
        FOREIGN KEY (VP_ID)
            REFERENCES virtual_person(ID)
;
ALTER TABLE virtual_person_signer_id
    ADD CONSTRAINT FK_VIRTUAL_PERSON_SIGNER_ID_SI_VALUE
        FOREIGN KEY
            (
             SI_VALUE,
             SIT_DOMAIN_NAME,
             SIT_NAME
                )
            REFERENCES signer_id
                (
                 VALUE,
                 SIT_DOMAIN_NAME,
                 SIT_NAME
                    )
;
CREATE UNIQUE INDEX I_PRIMARY ON virtual_person_signer_id
    (
     SIT_NAME,
     SIT_DOMAIN_NAME,
     SI_VALUE,
     VP_ID
        )
;
CREATE INDEX I_FK_VIRTUAL_PERSON_SIGNER_ID_SI_VALUE ON virtual_person_signer_id
    (
     SI_VALUE,
     SIT_DOMAIN_NAME,
     SIT_NAME
        )
;
CREATE INDEX I_FK_VIRTUAL_PERSON_SIGNER_ID_VP_ID ON virtual_person_signer_id(VP_ID)
;

CREATE TABLE `qc` (
                      `COMMENT` VARCHAR(4095) NULL DEFAULT NULL,
                      `TIMESTAMP` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                      `EXTERN_PROPERTIES` VARCHAR(4095) NULL DEFAULT NULL,
                      `INSPECTOR` VARCHAR(100) NULL DEFAULT NULL,
                      `VIRTUAL_PERSON_ID` BIGINT(20) NOT NULL,
                      `TYPE` VARCHAR(100) NOT NULL,
                      `CT_VERSION` INT(11) NOT NULL,
                      `CT_DOMAIN_NAME` VARCHAR(50) NOT NULL,
                      `CONSENT_DATE` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                      `CT_NAME` VARCHAR(100) NOT NULL,
                      `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT '',
                      PRIMARY KEY (`CONSENT_DATE`, `VIRTUAL_PERSON_ID`, `CT_DOMAIN_NAME`, `CT_NAME`, `CT_VERSION`) USING BTREE,
                      INDEX `I_qc_CONSENT_DATE` (`CONSENT_DATE`, `VIRTUAL_PERSON_ID`, `CT_DOMAIN_NAME`, `CT_NAME`, `CT_VERSION`) USING BTREE
) collate utf8_bin
;

ALTER TABLE qc
    ADD CONSTRAINT FK_qc_CONSENT_DATE
        FOREIGN KEY
            (
             CONSENT_DATE,
             VIRTUAL_PERSON_ID,
             CT_DOMAIN_NAME,
             CT_NAME,
             CT_VERSION
                )
            REFERENCES consent
                (
                 CONSENT_DATE,
                 VIRTUAL_PERSON_ID,
                 CT_DOMAIN_NAME,
                 CT_NAME,
                 CT_VERSION
                    )  ON UPDATE CASCADE ON DELETE CASCADE
;

CREATE TABLE `qc_hist` (
                           `COMMENT` VARCHAR(4095) NULL DEFAULT NULL,
                           `TIMESTAMP` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                           `EXTERN_PROPERTIES` VARCHAR(4095) NULL DEFAULT NULL,
                           `INSPECTOR` VARCHAR(100) NULL DEFAULT NULL,
                           `VIRTUAL_PERSON_ID` BIGINT(20) NOT NULL,
                           `TYPE` VARCHAR(100) NOT NULL,
                           `CT_VERSION` INT(11) NOT NULL,
                           `CT_DOMAIN_NAME` VARCHAR(50) NOT NULL,
                           `CONSENT_DATE` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                           `CT_NAME` VARCHAR(100) NOT NULL,
                           `START_DATE` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                           `END_DATE` TIMESTAMP(3) NULL DEFAULT NULL,
                           `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT '',
                           PRIMARY KEY (`CONSENT_DATE`, `VIRTUAL_PERSON_ID`, `CT_DOMAIN_NAME`, `CT_NAME`, `CT_VERSION`, `START_DATE`) USING BTREE,
                           INDEX `I_qc_CONSENT_DATE` (`CONSENT_DATE`, `VIRTUAL_PERSON_ID`, `CT_DOMAIN_NAME`, `CT_NAME`, `CT_VERSION`) USING BTREE
) collate utf8_bin
;

ALTER TABLE qc_hist
    ADD CONSTRAINT FK_qc_hist_CONSENT_DATE
        FOREIGN KEY
            (
             CONSENT_DATE,
             VIRTUAL_PERSON_ID,
             CT_DOMAIN_NAME,
             CT_NAME,
             CT_VERSION
                )
            REFERENCES consent
                (
                 CONSENT_DATE,
                 VIRTUAL_PERSON_ID,
                 CT_DOMAIN_NAME,
                 CT_NAME,
                 CT_VERSION
                    )  ON UPDATE CASCADE ON DELETE CASCADE
;

CREATE TABLE qc_problem
(
    `TYPE`              VARCHAR(100) NOT NULL,
    `REF`               VARCHAR(100) NOT NULL DEFAULT '',
    `STATUS`            VARCHAR(100) NULL,
    `FORM_VALUE`        VARCHAR(4095) NULL,
    `SCAN_VALUE`        VARCHAR(4095) NULL,
    `COMMENT_EXTERN`    VARCHAR(4095) NULL,
    `COMMENT_INTERN`    VARCHAR(4095) NULL,
    `CT_DOMAIN_NAME`    VARCHAR(50) NOT NULL,
    `CT_NAME`           VARCHAR(100) NOT NULL,
    `CT_VERSION`        INT(11) NOT NULL,
    `CONSENT_DATE`      TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `VIRTUAL_PERSON_ID` BIGINT(20) NOT NULL,
    `CREATE_TIMESTAMP`  TIMESTAMP(3)  DEFAULT CURRENT_TIMESTAMP(3),
    `UPDATE_TIMESTAMP`  TIMESTAMP(3)  DEFAULT CURRENT_TIMESTAMP(3),
    `FHIR_ID`           VARCHAR(41) NOT NULL DEFAULT '',
    PRIMARY KEY (`CT_DOMAIN_NAME`, `CT_NAME`, `CT_VERSION`, `CONSENT_DATE`, `VIRTUAL_PERSON_ID`, `TYPE`, `REF`) USING BTREE,
    INDEX I_qc_problem_qc (`CONSENT_DATE`, `VIRTUAL_PERSON_ID`, `CT_DOMAIN_NAME`, `CT_NAME`, `CT_VERSION`) USING BTREE
) collate utf8_bin
;

ALTER TABLE qc_problem
    ADD CONSTRAINT FK_qc_problem_qc
        FOREIGN KEY
            (
             CONSENT_DATE,
             VIRTUAL_PERSON_ID,
             CT_DOMAIN_NAME,
             CT_NAME,
             CT_VERSION
                )
            REFERENCES qc
                (
                 CONSENT_DATE,
                 VIRTUAL_PERSON_ID,
                 CT_DOMAIN_NAME,
                 CT_NAME,
                 CT_VERSION
                    ) ON UPDATE CASCADE ON DELETE CASCADE
;

CREATE TABLE qc_problem_hist
(
    `TYPE`              VARCHAR(100) NOT NULL,
    `REF`               VARCHAR(100) NOT NULL DEFAULT '',
    `STATUS`            VARCHAR(100) NULL,
    `FORM_VALUE`        VARCHAR(4095) NULL,
    `SCAN_VALUE`        VARCHAR(4095) NULL,
    `COMMENT_EXTERN`    VARCHAR(4095) NULL,
    `COMMENT_INTERN`    VARCHAR(4095) NULL,
    `CT_DOMAIN_NAME`    VARCHAR(50) NOT NULL,
    `CT_NAME`           VARCHAR(100) NOT NULL,
    `CT_VERSION`        INT(11) NOT NULL,
    `CONSENT_DATE`      TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `VIRTUAL_PERSON_ID` BIGINT(20) NOT NULL,
    `CREATE_TIMESTAMP`  TIMESTAMP(3)  DEFAULT CURRENT_TIMESTAMP(3),
    `UPDATE_TIMESTAMP`  TIMESTAMP(3)  DEFAULT CURRENT_TIMESTAMP(3),
    `START_DATE`        TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `END_DATE`          TIMESTAMP(3) NULL DEFAULT NULL,
    `FHIR_ID`           VARCHAR(41) NOT NULL DEFAULT '',
    PRIMARY KEY (`CT_DOMAIN_NAME`, `CT_NAME`, `CT_VERSION`, `CONSENT_DATE`, `VIRTUAL_PERSON_ID`, `TYPE`, `REF`, `START_DATE`) USING BTREE,
    INDEX I_qc_problem_hist_qc (`CONSENT_DATE`, `VIRTUAL_PERSON_ID`, `CT_DOMAIN_NAME`, `CT_NAME`, `CT_VERSION`) USING BTREE
) collate utf8_bin
;

ALTER TABLE qc_problem_hist
    ADD CONSTRAINT FK_qc_problem_hist_qc
        FOREIGN KEY
            (
             CONSENT_DATE,
             VIRTUAL_PERSON_ID,
             CT_DOMAIN_NAME,
             CT_NAME,
             CT_VERSION
                )
            REFERENCES qc
                (
                 CONSENT_DATE,
                 VIRTUAL_PERSON_ID,
                 CT_DOMAIN_NAME,
                 CT_NAME,
                 CT_VERSION
                    ) ON UPDATE CASCADE ON DELETE CASCADE
;

CREATE TABLE `alias` (
                         `CREATE_TIMESTAMP` timestamp(3) NOT NULL,
                         `ORIG_SI_VALUE` varchar(255) NOT NULL,
                         `ORIG_SIT_DOMAIN_NAME` varchar(50) NOT NULL,
                         `ORIG_SIT_NAME` varchar(100) NOT NULL,
                         `ALIAS_SI_VALUE` varchar(255) NOT NULL,
                         `ALIAS_SIT_DOMAIN_NAME` varchar(50) NOT NULL,
                         `ALIAS_SIT_NAME` varchar(100) NOT NULL,
                         `DEACTIVATE_TIMESTAMP` timestamp(3) NULL DEFAULT NULL,
                         PRIMARY KEY (`CREATE_TIMESTAMP`,`ALIAS_SI_VALUE`,`ORIG_SI_VALUE`,`ALIAS_SIT_NAME`,`ALIAS_SIT_DOMAIN_NAME`,`ORIG_SIT_DOMAIN_NAME`,`ORIG_SIT_NAME`)
) collate utf8_bin
;

ALTER TABLE alias
    ADD CONSTRAINT FK_ALIAS_SIGNER_ID
        FOREIGN KEY
            (
             ALIAS_SI_VALUE,
             ALIAS_SIT_DOMAIN_NAME,
             ALIAS_SIT_NAME
                )
            REFERENCES signer_id
                (
                 VALUE,
                 SIT_DOMAIN_NAME,
                 SIT_NAME
                    )
;

ALTER TABLE alias
    ADD CONSTRAINT FK_ORIG_SIGNER_ID
        FOREIGN KEY
            (
             ORIG_SI_VALUE,
             ORIG_SIT_DOMAIN_NAME,
             ORIG_SIT_NAME
                )
            REFERENCES signer_id
                (
                 VALUE,
                 SIT_DOMAIN_NAME,
                 SIT_NAME
                    )
;

-- -----------------------------------------------------
-- Table `stat_entry`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `stat_entry` ;

CREATE  TABLE IF NOT EXISTS `stat_entry` (
                                             `STAT_ENTRY_ID` BIGINT(20) NOT NULL AUTO_INCREMENT,
                                             `ENTRYDATE` TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
                                             PRIMARY KEY (`STAT_ENTRY_ID`) )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `stat_value`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `stat_value` ;

CREATE  TABLE IF NOT EXISTS `stat_value` (
                                             `stat_value_id` BIGINT(20) NULL DEFAULT NULL ,
                                             `stat_value` BIGINT(20) NULL DEFAULT NULL ,
                                             `stat_attr` VARCHAR(255) NULL DEFAULT NULL ,
                                             INDEX `FK_stat_value_stat_value_id` (`stat_value_id` ASC) ,
                                             CONSTRAINT `FK_stat_value_stat_value_id`
                                                 FOREIGN KEY (`stat_value_id` )
                                                     REFERENCES `stat_entry` (`STAT_ENTRY_ID` ))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;
