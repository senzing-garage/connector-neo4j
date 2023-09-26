# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
[markdownlint](https://dlaa.me/markdownlint/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.5.0] - 2023-09-26

### Changed in 0.5.0

- Updated `senzing-listener` dependency to version `0.5.0` (significant changes)
- Updated Dockerfile for adoptium.net Temurin 17 for consistency with other repos

## [0.2.2] - 2023-04-04

### Changed in 0.2.2

- In `Dockerfile`, updated FROM instruction to `senzing/senzingapi-tools:3.5.0`
- Added support for SENZING_ENGINE_CONFIGURATION_JSON environment variable.
- Update to use version 0.3.1 of senzing-listener.

## [0.2.1] - 2022-10-11

### Changed in 0.2.1

- In `Dockerfile`, updated FROM instruction to `senzing/senzingapi-tools:3.3.1`

## [0.2.0] - 2022-10-05

### Changed in 0.2.0

- In `Dockerfile`, updated FROM instruction to `senzing/senzingapi-tools:3.3.0`

## [0.1.2] - 2022-09-29

### Changed in 0.1.2

- In `Dockerfile`, updated FROM instruction to `senzing/senzing-base:1.6.12`

## [0.1.1] - 2022-08-09

### Added to 0.1.1

- Fixed Docker build scripts and updated README

## [0.1.0] - 2022-05-13

### Added to 0.1.0

- Updated project to match latest release of senzing-listener (v. 0.1.0) and G2 3.0.0.
