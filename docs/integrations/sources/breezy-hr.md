# Breezy HR
An Airbyte source for Breezy applicant tracking system.
## Configuration

| Input | Type | Description | Default Value |
|-------|------|-------------|---------------|
| `api_key` | `string` | API Key.  |  |
| `company_id` | `string` | Company ID.  |  |

## Streams
| Stream Name | Primary Key | Pagination | Supports Full Sync | Supports Incremental |
|-------------|-------------|------------|---------------------|----------------------|
| positions |  | No pagination | ✅ |  ❌  |
| candidates |  | No pagination | ✅ |  ❌  |
| pipelines |  | No pagination | ✅ |  ❌  |


## Changelog

<details>
  <summary>Expand to review</summary>

| Version | Date | Pull Request | Subject |
|---------|------|--------------|---------|
| 0.0.26 | 2025-06-28 | [62150](https://github.com/airbytehq/airbyte/pull/62150) | Update dependencies |
| 0.0.25 | 2025-06-21 | [61874](https://github.com/airbytehq/airbyte/pull/61874) | Update dependencies |
| 0.0.24 | 2025-05-24 | [59901](https://github.com/airbytehq/airbyte/pull/59901) | Update dependencies |
| 0.0.23 | 2025-05-03 | [59360](https://github.com/airbytehq/airbyte/pull/59360) | Update dependencies |
| 0.0.22 | 2025-04-26 | [58747](https://github.com/airbytehq/airbyte/pull/58747) | Update dependencies |
| 0.0.21 | 2025-04-19 | [58265](https://github.com/airbytehq/airbyte/pull/58265) | Update dependencies |
| 0.0.20 | 2025-04-12 | [57663](https://github.com/airbytehq/airbyte/pull/57663) | Update dependencies |
| 0.0.19 | 2025-04-05 | [57118](https://github.com/airbytehq/airbyte/pull/57118) | Update dependencies |
| 0.0.18 | 2025-03-29 | [56584](https://github.com/airbytehq/airbyte/pull/56584) | Update dependencies |
| 0.0.17 | 2025-03-22 | [55407](https://github.com/airbytehq/airbyte/pull/55407) | Update dependencies |
| 0.0.16 | 2025-03-01 | [54854](https://github.com/airbytehq/airbyte/pull/54854) | Update dependencies |
| 0.0.15 | 2025-02-22 | [54211](https://github.com/airbytehq/airbyte/pull/54211) | Update dependencies |
| 0.0.14 | 2025-02-15 | [53906](https://github.com/airbytehq/airbyte/pull/53906) | Update dependencies |
| 0.0.13 | 2025-02-08 | [53385](https://github.com/airbytehq/airbyte/pull/53385) | Update dependencies |
| 0.0.12 | 2025-02-01 | [52885](https://github.com/airbytehq/airbyte/pull/52885) | Update dependencies |
| 0.0.11 | 2025-01-25 | [52168](https://github.com/airbytehq/airbyte/pull/52168) | Update dependencies |
| 0.0.10 | 2025-01-18 | [51736](https://github.com/airbytehq/airbyte/pull/51736) | Update dependencies |
| 0.0.9 | 2025-01-11 | [51248](https://github.com/airbytehq/airbyte/pull/51248) | Update dependencies |
| 0.0.8 | 2024-12-28 | [50481](https://github.com/airbytehq/airbyte/pull/50481) | Update dependencies |
| 0.0.7 | 2024-12-21 | [50198](https://github.com/airbytehq/airbyte/pull/50198) | Update dependencies |
| 0.0.6 | 2024-12-14 | [49547](https://github.com/airbytehq/airbyte/pull/49547) | Update dependencies |
| 0.0.5 | 2024-12-12 | [49315](https://github.com/airbytehq/airbyte/pull/49315) | Update dependencies |
| 0.0.4 | 2024-12-11 | [49020](https://github.com/airbytehq/airbyte/pull/49020) | Starting with this version, the Docker image is now rootless. Please note that this and future versions will not be compatible with Airbyte versions earlier than 0.64 |
| 0.0.3 | 2024-10-29 | [47750](https://github.com/airbytehq/airbyte/pull/47750) | Update dependencies |
| 0.0.2 | 2024-10-28 | [47587](https://github.com/airbytehq/airbyte/pull/47587) | Update dependencies |
| 0.0.1 | 2024-08-20 | | Initial release by natikgadzhi via Connector Builder |

</details>
