# Changelog

## [1.2.1](https://github.com/danielscholl-osdu/legal/compare/v1.2.0...v1.2.1) (2026-09-26)


### 📚 Documentation

* Add CONTRIBUTING.md and update infrastructure link ([f91ce0a](https://github.com/danielscholl-osdu/legal/commit/f91ce0a4d4392fb89fdd744cbc626f48afce4257))
* Add CONTRIBUTING.md and update infrastructure link ([63ab153](https://github.com/danielscholl-osdu/legal/commit/63ab15348351b0fe7489f3f5e7bb907f5b28616d))


### 🔧 Miscellaneous

* Sync template updates ([353ecec](https://github.com/danielscholl-osdu/legal/commit/353ecec99ac5057571dfbbbdf316d89ef02bf8e2))
* **template-sync:** Sync template updates (updated 2026-09-26) ([fa6c0ee](https://github.com/danielscholl-osdu/legal/commit/fa6c0ee6d065ee51da0305528832c9ff043ab77e))

## [1.2.0](https://github.com/danielscholl-osdu/legal/compare/v1.1.0...v1.2.0) (2026-09-23)


### ✨ Features

* Add vendor-neutral OIDC authentication support for acceptance tests ([a149b37](https://github.com/danielscholl-osdu/legal/commit/a149b373c94789b8fe0081ba7c1a406e2ed960f6))
* Add vendor-neutral OIDC authentication support for acceptance tests ([a604bec](https://github.com/danielscholl-osdu/legal/commit/a604bec7a59e0de6d195dfbaf03b720bd51ab49a))
* **azure:** Switch Redis authentication from password to MSI ([55e49e0](https://github.com/danielscholl-osdu/legal/commit/55e49e024b98e3aeabe69c42724750e84f5c97fe))
* **azure:** Switch Redis authentication from password to MSI ([7ee7c60](https://github.com/danielscholl-osdu/legal/commit/7ee7c60d90630d5d32e1975b4bb61334087472b9))
* Connection optimization ([2971933](https://github.com/danielscholl-osdu/legal/commit/2971933f370f1dca4b17f99b22dfc7e438bd5518))
* Connection optimization ([dda31eb](https://github.com/danielscholl-osdu/legal/commit/dda31eb6f1b6ca287a8222ecb022d88988a1637a))
* **spi:** Add the acceptance service descriptor ([f7caa91](https://github.com/danielscholl-osdu/legal/commit/f7caa91e910f86e11f50faeff92497e44bf96573))
* **spi:** Add the acceptance service descriptor ([a578cd3](https://github.com/danielscholl-osdu/legal/commit/a578cd385fa0ffb05de1004f1772f236fb971083))


### 🐛 Bug Fixes

* Aws build after POM split ([4d31796](https://github.com/danielscholl-osdu/legal/commit/4d31796d95dc32ff60bb79251c7633f3222be36c))
* Aws build after POM split ([1db32cc](https://github.com/danielscholl-osdu/legal/commit/1db32cc8bfe4876dc9716bbb90066b64c6a67926))
* **azure:** Netty-bom before core-lib-azure (lettuce 7.5.2 NoClassDefFoundError) ([fd44e4c](https://github.com/danielscholl-osdu/legal/commit/fd44e4c86d3dfa7c08938f1facb2b4a2be4b62cc))
* **azure:** Netty-bom before core-lib-azure (lettuce 7.5.2 NoClassDefFoundError) ([b27e35d](https://github.com/danielscholl-osdu/legal/commit/b27e35df5835fea53cf4c9d93282b2101ab536f8))
* **azure:** Upgrade core-lib-azure to 3.0.1 ([6f6f6e0](https://github.com/danielscholl-osdu/legal/commit/6f6f6e06be7f3f85cb610f4c21f57efd64f2d96b))
* **azure:** Upgrade core-lib-azure to 3.0.1 ([ed7ac34](https://github.com/danielscholl-osdu/legal/commit/ed7ac34fb82c7ad3f127b29d3c9efe6270ec09d0))
* **cimpl:** Enable feature-flag asserts on cimpl-dev2-acceptance-test ([d1d96a9](https://github.com/danielscholl-osdu/legal/commit/d1d96a9a99f1e306cc77caaa07224e0f50c902cf))
* **cimpl:** Enable feature-flag asserts on cimpl-dev2-acceptance-test ([fc52bef](https://github.com/danielscholl-osdu/legal/commit/fc52bef73e0a950f0e68f27f49e3e55c62690c46))
* **cimpl:** Pin master chart images to :latest in local pipeline override ([a955c77](https://github.com/danielscholl-osdu/legal/commit/a955c7756a29f6434fb86ddf93339fbb8e2bb826))
* **cimpl:** Pin master chart images to :latest in local pipeline override ([d59ef21](https://github.com/danielscholl-osdu/legal/commit/d59ef213c382d36573b5b8585e884d0ff5cb1211))
* **core-plus:** Pin legal status-update image to :latest and rename to cimpl-legal-status-update ([96eb366](https://github.com/danielscholl-osdu/legal/commit/96eb36665e1e40f8e9ddbea8b27dfbe26c339603))
* **core-plus:** Pin legal status-update image to :latest and rename to cimpl-legal-status-update ([6447974](https://github.com/danielscholl-osdu/legal/commit/64479740a558fff94d59b6a93bacf79186b105e8))
* Cve and spring boot version bump ([d36f796](https://github.com/danielscholl-osdu/legal/commit/d36f796a4790a44feb007745170c8f047a54ec6e))
* Cve and spring boot version bump ([ff47afc](https://github.com/danielscholl-osdu/legal/commit/ff47afc5a5b2ea749672c5f7f79b40610ab2f57f))
* **cve:** Pom cleanup + spring-boot 3.5.16 CVE remediation ([103c388](https://github.com/danielscholl-osdu/legal/commit/103c38891cc36bc4959704865d1f1ee60647cda1))
* **cve:** Pom cleanup + spring-boot 3.5.16 CVE remediation ([dcc2582](https://github.com/danielscholl-osdu/legal/commit/dcc2582ba8e137b273af485c9d2561fcf27e5f26))
* **deps:** Upgrade plexus-utils to 4.1.0 to fix directory traversal (CVE-2025-67030) ([3d7686d](https://github.com/danielscholl-osdu/legal/commit/3d7686d626644f49cdd4863a69db890c64e94e1d))
* **deps:** Upgrade plexus-utils to 4.1.0 to fix directory traversal (CVE-2025-67030) ([c1a27bd](https://github.com/danielscholl-osdu/legal/commit/c1a27bde75fd78ed6ddd0f48380cb2eef34efe09)), closes [#192](https://github.com/danielscholl-osdu/legal/issues/192)
* Legal tag filtering test ([79efc9d](https://github.com/danielscholl-osdu/legal/commit/79efc9d2d289e86010381d4df77f54bf788f117d))
* Legal tag filtering test ([f0c1809](https://github.com/danielscholl-osdu/legal/commit/f0c1809451b98fdf5cc4f0b6fc92474f9248365e))
* Moving script in buildspec instead ([132840a](https://github.com/danielscholl-osdu/legal/commit/132840a39fff0896d987cd3fddb48645e1cb33da))
* Spring-core tomcat netty version bump ([e9cd10e](https://github.com/danielscholl-osdu/legal/commit/e9cd10e632d09019642df40fdaa99cc043ed2502))
* Spring-core tomcat netty version bump ([d1a81d0](https://github.com/danielscholl-osdu/legal/commit/d1a81d020c1ff8814c0b3ce06745fdb489b1ebc5))
* Sync upstream changes from 1fc4c683 ([6702344](https://github.com/danielscholl-osdu/legal/commit/6702344cffd91f799f022e609763ef8a16fefb01))
* Tomcat-core crypto json-smart netty-common CVE ([0d7dd56](https://github.com/danielscholl-osdu/legal/commit/0d7dd56fca427b7c4e92906e6f2e377dcc734493))
* Tomcat-core crypto json-smart netty-common CVE ([80c6f55](https://github.com/danielscholl-osdu/legal/commit/80c6f55c09e628b10175031139c0e93eed856ec0))
* Tomcat-core CVE ([cc7ac34](https://github.com/danielscholl-osdu/legal/commit/cc7ac3415cac739063bad9bbef4d845aff734bcf))
* Tomcat-core CVE ([cdeaac0](https://github.com/danielscholl-osdu/legal/commit/cdeaac07de3af4539962ce01aba220734f15c89d))
* Updated ReadMe ([b15f707](https://github.com/danielscholl-osdu/legal/commit/b15f7073a0311356b1613834514f9c0350dee7e7))
* Updated ReadMe ([4d48d7c](https://github.com/danielscholl-osdu/legal/commit/4d48d7c5642cd60e6dd9f1a5e4df7a6d250b2e37))


### 🔧 Miscellaneous

* **ci:** Remove IBM jobs from pipeline ([ac9134d](https://github.com/danielscholl-osdu/legal/commit/ac9134d07e3b0c84fe92a0a61e9bddc3e2b132f4))
* **ci:** Remove IBM jobs from pipeline ([579e581](https://github.com/danielscholl-osdu/legal/commit/579e58124cbcb8d0c09ce83a32591372919e1a1d))
* Complete repository initialization ([2c4d23b](https://github.com/danielscholl-osdu/legal/commit/2c4d23b1bac074eee3aae3a54f4596c42fb8058e))
* Copy configuration and workflows from main branch ([a078d3e](https://github.com/danielscholl-osdu/legal/commit/a078d3e9da1170f6ed86abd19034590febc67cda))
* Deleting aws helm chart ([8a32cb8](https://github.com/danielscholl-osdu/legal/commit/8a32cb8176717fbe306f06546167013cfcef1caf))
* Deleting aws helm chart ([2762acf](https://github.com/danielscholl-osdu/legal/commit/2762acfbdd67f7962a37db02c3b3ae9e7c4819a4))
* **deps:** Apply security updates and sync with os-core-common ([e1e17e5](https://github.com/danielscholl-osdu/legal/commit/e1e17e5f61c84119434b9afa9f4d34c1a9c0a12b))
* **deps:** Apply security updates and sync with os-core-common ([8b337e9](https://github.com/danielscholl-osdu/legal/commit/8b337e92d3712b10f008d3b5433deb0fb1edb916))
* **deps:** Dependency bumps ([2a669ef](https://github.com/danielscholl-osdu/legal/commit/2a669ef3e6d1bc5119f10539c98fb62e04e6c796))
* **deps:** Dependency bumps ([71ab771](https://github.com/danielscholl-osdu/legal/commit/71ab7713687e05adeb422438d418ad35efb7d74b))
* **deps:** Security dependency remediation - Spring Boot 3.5.8 and library updates ([648e4cd](https://github.com/danielscholl-osdu/legal/commit/648e4cdaa7eac32591120243d7d903dad040b7b4))
* **deps:** Security dependency remediation - Spring Boot 3.5.8 and library updates ([8b8297d](https://github.com/danielscholl-osdu/legal/commit/8b8297d894fa859b6289aea4486a5d3bd5c4174b))
* **gc:** Decouple GCP provider (GONRG-15184) ([1fc4c68](https://github.com/danielscholl-osdu/legal/commit/1fc4c6835e1e00775f3063207eb24489f6f10749))
* **gc:** Decouple GCP provider (GONRG-15184) ([6f7e57e](https://github.com/danielscholl-osdu/legal/commit/6f7e57ed9a393f80c3289c65cfeff5176045bb56))
* Generate filtered upstream tree ([93e9ec5](https://github.com/danielscholl-osdu/legal/commit/93e9ec52afbdba2e4e52957825498d6f06281f03))
* Generate filtered upstream tree ([f782e93](https://github.com/danielscholl-osdu/legal/commit/f782e9317b30751faeecebc2ec27e96fa9f81a1c))
* Refresh FOSSA NOTICE ([3e776cd](https://github.com/danielscholl-osdu/legal/commit/3e776cd73318d317441c8908713d28411140faf9))
* Remove AWS provider and AWS CI/CD ([f519624](https://github.com/danielscholl-osdu/legal/commit/f519624b77d563bf0a553e915ab5c8080e8d2e69))
* Remove AWS provider and AWS CI/CD ([0f215d4](https://github.com/danielscholl-osdu/legal/commit/0f215d4ad00a3e6e282b0072f336d65553b70eda))
* Removing helm copy from aws buildspec ([8473fb3](https://github.com/danielscholl-osdu/legal/commit/8473fb36a10d3796b338d17f84aa2795b280d663))
* Seed fork-owned azure trees ([243bfb6](https://github.com/danielscholl-osdu/legal/commit/243bfb6e69dff35f2be9a162a2fb103dc0e44638))
* Sync template updates ([98eafc6](https://github.com/danielscholl-osdu/legal/commit/98eafc603b8e192f87faf9763209df8849378e19))
* Sync template updates ([696a439](https://github.com/danielscholl-osdu/legal/commit/696a4390046e1e4e37b9815d29f4522b3ac4fbe0))
* **template-sync:** Sync template updates (updated 2026-09-23) ([93e0622](https://github.com/danielscholl-osdu/legal/commit/93e06229f34859b78db7fef476ffcc4c1d398428))
* **template-sync:** Sync template updates 2026-09-16 ([19d0f59](https://github.com/danielscholl-osdu/legal/commit/19d0f59dfa22d73ab683f7dc7714c3230112ce2b))


### ♻️ Code Refactoring

* **audit:** Encapsulate audit roles in logging layer ([2e82d59](https://github.com/danielscholl-osdu/legal/commit/2e82d5975a49564b64574190e492af403164637d))
* **audit:** Encapsulate audit roles in logging layer ([7bcf4f4](https://github.com/danielscholl-osdu/legal/commit/7bcf4f48b07108ce9c16055fb541b422dbaa4b2d))


### 🔨 Build System

* **load:** Add load test Dockerfile and entrypoint ([2b07b83](https://github.com/danielscholl-osdu/legal/commit/2b07b838727390eafb39610ff73c456af714ebc9))
* Remove redundant version declarations from child modules ([8c736eb](https://github.com/danielscholl-osdu/legal/commit/8c736ebb86c40aa12895c867e5a65db35d52df0f))
* Remove redundant version declarations from child modules ([8ac5dfa](https://github.com/danielscholl-osdu/legal/commit/8ac5dfa55ce044c832667255051fb28b652c3221))


### ⚙️ Continuous Integration

* **fix:** Adding cimpl-prod-acceptance-test overrides ([6fa16fd](https://github.com/danielscholl-osdu/legal/commit/6fa16fd3e75668b72a8d7af27c6c6f29917f9512))
* **gc:** Fix unset legalStatusUpdateImage in GCR release pipeline (GONRG-13893) ([e00128a](https://github.com/danielscholl-osdu/legal/commit/e00128ab9879c19f47058ca939efa15ebc7fd406))
* **gc:** Fix unset legalStatusUpdateImage in GCR release pipeline (GONRG-13893) ([a7845b6](https://github.com/danielscholl-osdu/legal/commit/a7845b60c26541def1db5bab6646231b45bbded1))
