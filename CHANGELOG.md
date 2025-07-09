# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 1.0.0 (2025-07-09)


### 🐛 Bug Fixes

* Aws build after POM split ([4d31796](https://github.com/danielscholl-osdu/legal/commit/4d31796d95dc32ff60bb79251c7633f3222be36c))
* Aws build after POM split ([1db32cc](https://github.com/danielscholl-osdu/legal/commit/1db32cc8bfe4876dc9716bbb90066b64c6a67926))
* Moving script in buildspec instead ([132840a](https://github.com/danielscholl-osdu/legal/commit/132840a39fff0896d987cd3fddb48645e1cb33da))
* Tomcat-core crypto json-smart netty-common CVE ([0d7dd56](https://github.com/danielscholl-osdu/legal/commit/0d7dd56fca427b7c4e92906e6f2e377dcc734493))
* Tomcat-core crypto json-smart netty-common CVE ([80c6f55](https://github.com/danielscholl-osdu/legal/commit/80c6f55c09e628b10175031139c0e93eed856ec0))
* Updated ReadMe ([b15f707](https://github.com/danielscholl-osdu/legal/commit/b15f7073a0311356b1613834514f9c0350dee7e7))
* Updated ReadMe ([4d48d7c](https://github.com/danielscholl-osdu/legal/commit/4d48d7c5642cd60e6dd9f1a5e4df7a6d250b2e37))


### 📚 Documentation

* Updating helm chart documentation and versioning ([0fb9acb](https://github.com/danielscholl-osdu/legal/commit/0fb9acb39e0c7417b415fad37580fa7bc3e3b794))


### 🔧 Miscellaneous

* Complete repository initialization ([7886a6d](https://github.com/danielscholl-osdu/legal/commit/7886a6d92b31f9a139700840d7947ffddea0d783))
* Copy configuration and workflows from main branch ([0e94b99](https://github.com/danielscholl-osdu/legal/commit/0e94b9948dc2654fbdd495d5fb20d846a3faf389))
* Deleting aws helm chart ([8a32cb8](https://github.com/danielscholl-osdu/legal/commit/8a32cb8176717fbe306f06546167013cfcef1caf))
* Deleting aws helm chart ([2762acf](https://github.com/danielscholl-osdu/legal/commit/2762acfbdd67f7962a37db02c3b3ae9e7c4819a4))
* Removing helm copy from aws buildspec ([8473fb3](https://github.com/danielscholl-osdu/legal/commit/8473fb36a10d3796b338d17f84aa2795b280d663))

## [2.0.0] - Major Workflow Enhancement & Documentation Release

### ✨ Features
- **Comprehensive MkDocs Documentation Site**: Complete documentation overhaul with GitHub Pages deployment
- **Automated Cascade Failure Recovery**: System automatically recovers from cascade workflow failures
- **Human-Centric Cascade Pattern**: Issue lifecycle tracking with human notifications for critical decisions
- **Integration Validation**: Comprehensive validation system for cascade workflows
- **Claude Workflow Integration**: Full Claude Code CLI support with Maven MCP server integration
- **GitHub Copilot Enhancement**: Java development environment setup and firewall configuration
- **Fork Resources Staging Pattern**: Template-based staging for fork-specific configurations
- **Conventional Commits Validation**: Complete validation system with all supported commit types
- **Enhanced PR Label Management**: Simplified production PR labels with automated issue closure
- **Meta Commit Strategy**: Advanced release-please integration for better version management
- **Push Protection Handling**: Sophisticated upstream secrets detection and resolution workflows

### 🔨 Build System
- **Workflow Separation Pattern**: Template development vs. fork instance workflow isolation
- **Template Workflow Management**: 9 comprehensive template workflows for fork management
- **Enhanced Action Reliability**: Improved cascade workflow trigger reliability with PR event filtering
- **Base64 Support**: Enhanced create-enhanced-pr action with encoding capabilities

### 📚 Documentation
- **Structured MkDocs Site**: Complete documentation architecture with GitHub Pages
- **AI-First Development Docs**: Comprehensive guides for AI-enhanced development
- **ADR Documentation**: 20+ Architectural Decision Records covering all major decisions
- **Workflow Specifications**: Detailed documentation for all 9 template workflows
- **Streamlined README**: Focused quick-start guide directing to comprehensive documentation

### 🛡️ Security & Reliability
- **Advanced Push Protection**: Intelligent handling of upstream repositories with secrets
- **Branch Protection Integration**: Automated branch protection rule management
- **Security Pattern Recognition**: Enhanced security scanning and pattern detection
- **MCP Configuration**: Secure Model Context Protocol integration for AI development

### 🔧 Workflow Enhancements
- **Cascade Monitoring**: Advanced cascade workflow monitoring and SLA management
- **Dependabot Integration**: Enhanced dependabot validation and automation
- **Template Synchronization**: Sophisticated template update propagation system
- **Issue State Tracking**: Advanced issue lifecycle management and tracking
- **GITHUB_TOKEN Standardization**: Improved token handling across all workflows

### ♻️ Code Refactoring
- **Removed AI_EVOLUTION.md**: Migrated to structured ADR approach for better maintainability
- **Simplified README Structure**: Eliminated redundancy between README and documentation site
- **Enhanced Initialization Cleanup**: Improved fork repository cleanup and setup process
- **Standardized Error Handling**: Consistent error handling patterns across all workflows

### 🐛 Bug Fixes
- **YAML Syntax Issues**: Resolved multiline string handling in workflow configurations
- **Release Workflow Compatibility**: Updated to googleapis/release-please-action@v4
- **MCP Server Configuration**: Fixed Maven MCP server connection and configuration issues
- **Cascade Trigger Reliability**: Implemented pull_request_target pattern for better triggering
- **Git Diff Syntax**: Corrected git command syntax in sync-template workflow
- **Label Management**: Standardized label usage across all workflows and templates

## [1.0.0] - Initial Release

### ✨ Features
- Initial release of OSDU Fork Management Template
- Automated fork initialization workflow
- Daily upstream synchronization with AI-enhanced PR descriptions
- Three-branch management strategy (main, fork_upstream, fork_integration)
- Automated conflict detection and resolution guidance
- Semantic versioning and release management
- Template development workflows separation

### 📚 Documentation
- Complete architectural decision records (ADRs)
- Product requirements documentation
- Development and usage guides
- GitHub Actions workflow documentation
