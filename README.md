# 🪙 Cara ou Coroa - Android App

Um aplicativo Android moderno e interativo de **Cara ou Coroa**, desenvolvido em **Kotlin** utilizando **Jetpack Compose** e **Material Design 3**. O app conta com animações 3D de giro da moeda, suporte a lançamentos únicos e múltiplos, controle de repetições consecutivas, efeitos sonoros e placar acumulativo.

---

## 🚀 Funcionalidades

- 🪙 **Simulação com Animação 3D**: Animação fluida de rotação da moeda no eixo Y usando `graphicsLayer` e `animateFloatAsState`.
- 🎯 **Modo Único (1 por 1)**:
  - **Proteção Contra Repetições Longas**: Opção configurável para limitar o número máximo de resultados consecutivos iguais (ex: evitar mais de 3 Caras seguidas).
- 🎲 **Modo Múltiplo**:
  - Lançamento em lote de múltiplos giros de uma só vez.
  - Botões com incremento/decremanto rápido ao manter pressionado (*Auto Repeat*).
- 📊 **Placar em Tempo Real**:
  - Contagem acumulada total de Caras e Coroas.
  - Opção para **Zerar Placar** a qualquer momento.
- 🔊 **Efeitos Sonoros**:
  - Áudio de lançamento da moeda com botão para alternar (ativar/desativar som).
- 🎨 **Interface Moderna**:
  - Construído 100% em **Jetpack Compose** e **Material Design 3**.
  - Suporte a **Edge-to-Edge** (layout integrado com as barras do sistema).
  - Ícone adaptativo do aplicativo (*Adaptive Icon*).

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Design System**: [Material Design 3](https://m3.material.io/)
- **Animações**: Compose Animation APIs (`animateFloatAsState`, `graphicsLayer`)
- **Efeitos Sonoros**: `MediaPlayer` API
- **Arquitetura/Componentes**: State Management com `remember` e `mutableStateOf`

---

## 📱 Demonstração

*(Dica: Adicione aqui capturas de tela ou GIFs do app em funcionamento)*

| Tela Inicial | Modo Múltiplo |
| :---: | :---: |
| *[Adicionar Screenshot]* | *[Adicionar Screenshot]* |

---

## 🔧 Como Executar o Projeto

1. **Pré-requisitos**:
   - [Android Studio](https://developer.android.com/studio) (versão recomendada: Android Studio Jellyfish / Ladybug ou superior).
   - JDK 17 ou superior.
   - Dispositivo Android físico ou emulador com API 24 (Android 7.0) ou superior.

2. **Passos para rodar**:
   ```bash
   # Clone o repositório
   git clone https://github.com/SEU_USUARIO/cara-ou-coroa.git

   # Acesse o diretório do projeto
   cd cara-ou-coroa
   ```

3. Abra o projeto no **Android Studio**.
4. Aguarde o **Gradle Sync** terminar.
5. Selecione seu dispositivo/emulador e clique no botão **Run** (ou pressione `Shift + F10`).

---

## 📂 Estrutura de Pastas

```text
app/src/main/
├── java/com/example/caraoucoroa/
│   ├── MainActivity.kt          # Interface do app, lógica de sorteio, animação e som
│   └── ui/theme/                # Temas, cores e tipografia (Material 3)
└── res/
    ├── drawable/                # Imagens da moeda (moeda_cara, moeda_coroa)
    ├── raw/                     # Arquivos de áudio (coin_flip)
    └── mipmap/                  # Ícones adaptativos do aplicativo
```

---

## 📄 Licença

Este projeto está sob a licença [Apache](LICENSE) - consulte o arquivo de licença para obter mais detalhes.

---

Feito com ❤️ por [DevmanCrybaby](https://github.com/SEU_USUARIO).
