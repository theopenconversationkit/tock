[![Build Status](https://app.travis-ci.com/theopenconversationkit/tock.png)](https://app.travis-ci.com/github/theopenconversationkit/tock)
[![Maven Central](https://img.shields.io/maven-central/v/ai.tock/tock-root.svg)](https://search.maven.org/search?q=tock)
[![Release Date](https://img.shields.io/github/release-date/theopenconversationkit/tock)](https://github.com/theopenconversationkit/tock/releases)

[![Gitter](https://badges.gitter.im/tockchat/Lobby.svg)](https://gitter.im/tockchat/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=body_badge)
[![Contributors](https://img.shields.io/github/contributors-anon/theopenconversationkit/tock)](https://github.com/theopenconversationkit/tock/graphs/contributors)
[![Commit Activity](https://img.shields.io/github/commit-activity/m/theopenconversationkit/tock)](https://github.com/theopenconversationkit/tock/pulse/monthly)

[![Home](https://img.shields.io/website?label=home&down_message=offline&up_message=doc.tock.ai&url=https%3A%2F%2Fdoc.tock.ai)](https://doc.tock.ai)
[![Demo](https://img.shields.io/badge/demo-live-blue)](https://demo.tock.ai)
[![License](https://img.shields.io/github/license/theopenconversationkit/tock)](https://github.com/theopenconversationkit/tock/blob/master/LICENSE)

# Tock - The Open Conversation Kit

<img alt="Tock Logo" src="http://doc.tock.ai/tock/master/img/logo.svg" style="width: 150px;"><br>

_Curious about what Tock is or, who is using it? Check out our [website](https://doc.tock.ai)!_

Open Conversational AI platform to build Bots:

* _Natural Language Processing_ open source stack, compatible with OpenNLP, Stanford, Rasa and more
* _Tock Studio_ user interface to build stories and analytics
* _Conversational DSL_ for Kotlin, Nodejs, Python and REST API
* _Built-in connectors_ for numerous text/voice channels: Messenger, WhatsApp, Google Assistant, Alexa, Twitter and more
* _Provided toolkits_ for custom Web/Mobile integration with React and Flutter
* _Deploy anywhere_ in the Cloud or On-Premise with Docker
 
🏠 Home: [https://doc.tock.ai](https://doc.tock.ai)
 
🕮 Documentation: [https://doc.tock.ai/tock/master/](https://doc.tock.ai/tock/master/)

🐋 Docker configurations: [https://github.com/theopenconversationkit/tock-docker](https://github.com/theopenconversationkit/tock-docker)

▶️ Live demo: [https://demo.tock.ai](https://demo.tock.ai)
  
💬 Contact: [https://gitter.im/tockchat/Lobby](https://gitter.im/tockchat/Lobby)

🔢 Versions: [https://gitter.im/tockchat/tock-news](https://gitter.im/tockchat/tock-news)

## Kotlin developers : use ktlint

In order to format your code with [ktlint](https://pinterest.github.io/ktlint):
```bash
mvn antrun:run@ktlint-format
```

You may need to build snapshot before:
```bash
mvn install -Dktlint.fail=false
```


## Python developers : use Pre-commit

The python part of the project uses **pre-commit** to automate code checks and formatting before each commit, ensuring consistent code quality and reducing errors.<br/>
It is very important to always execute these hooks to maintain the quality of the code.

### Installation
1. Install `pre-commit`:
   ```bash
   pip install pre-commit
   ```
2. Set up the hooks in your repository:
   ```bash
    pre-commit install
   ```
### Usage

Hooks will run automatically on each commit.<br/>
To run them manually on all files, use:
   ```bash
   pre-commit run --all-files
   ```


## 贡献指南

欢迎贡献！请查看[CONTRIBUTING.md](CONTRIBUTING.md)了解详细指南。

### 开发流程
1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建Pull Request

### 代码规范
- 遵循PEP 8 (Python)或相应语言规范
- 添加适当的注释
- 编写测试用例
- 更新相关文档