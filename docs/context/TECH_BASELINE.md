# Baseline tecnológica

Data de verificação: **2026-07-30**

Este documento fixa um ponto de partida, não uma obrigação de perseguir versões novas durante a implementação. Atualize uma versão somente por incompatibilidade, correção relevante ou necessidade do projeto, registrando o motivo.

## Ambiente de build

| Item | Baseline | Observação |
|---|---:|---|
| Android Studio | Quail 2, 2026.1.2 | versão estável pesquisada |
| JDK | 17 | exigido/compatível com o toolchain escolhido |
| Android Gradle Plugin | 9.3.0 | usa built-in Kotlin por padrão |
| Gradle | 9.5.0 | baseline compatível com AGP 9.3 |
| compileSdk | 36 | Android 16 API 36 |
| targetSdk | 36 | alvo da entrega |
| minSdk | 23 | compatível com Koin 4.2 |
| KSP | 2.3.10 | processador para Room; validar no primeiro build |

### Built-in Kotlin

Com AGP 9, não aplique automaticamente `org.jetbrains.kotlin.android`. O suporte Kotlin vem integrado ao AGP. Use KSP no lugar de kapt para Room. Plugins adicionais de Kotlin devem ser adicionados somente quando realmente necessários e compatíveis.

## Bibliotecas

| Biblioteca | Baseline | Uso |
|---|---:|---|
| Compose BOM | 2026.06.00 | versões coordenadas do Compose |
| Lifecycle | 2.11.0 | ViewModel, LiveData e coleta lifecycle-aware |
| Navigation | 2.9.8 | Navigation Component/Fragments |
| Paging | 3.5.0 | paginação e integração Compose |
| Room | 2.8.4 | favoritos locais |
| Retrofit | 3.0.0 | API HTTP |
| Coil | 3.5.0 | imagens Compose/Views |
| Koin BOM | 4.2.1 | DI Android, Compose e ViewModel |
| Kotlin serialization JSON | 1.11.0 | parsing JSON |

Dependências adicionais esperadas, sem versão duplicada quando cobertas por BOM:

- Material 3;
- `lifecycle-runtime-compose`;
- `navigation-fragment-ktx` e `navigation-ui-ktx`;
- `paging-runtime` e `paging-compose`;
- `room-runtime`, `room-ktx`, `room-paging` se necessário e compiler via KSP;
- `retrofit` e conversor Kotlin serialization;
- OkHttp logging apenas em debug e com headers sensíveis redigidos;
- `coil-compose`, `coil-network-okhttp` e suporte a Views caso a tela XML carregue imagem diretamente;
- `koin-android`, `koin-compose`/`koin-compose-viewmodel` conforme APIs escolhidas;
- `kotlinx-coroutines-test` e `paging-testing` para testes.

## Catálogo de versões

O projeto deve usar `gradle/libs.versions.toml` para centralizar bibliotecas e plugins. Não replique números de versão em múltiplos arquivos.

Antes de codificar features, a fase de bootstrap deve provar:

1. sync do Gradle;
2. compilação Kotlin;
3. geração KSP do Room;
4. preview/compilação Compose;
5. inicialização Koin;
6. teste unitário simples.

Se uma incompatibilidade ocorrer, ajuste o menor conjunto possível e registre em `CONTEXT.md`:

- versão anterior;
- versão escolhida;
- erro observado;
- fonte oficial ou evidência do build.

## Referências oficiais

- Android Studio releases: https://developer.android.com/studio/releases
- Android Gradle Plugin 9.3: https://developer.android.com/build/releases/gradle-plugin
- Built-in Kotlin: https://developer.android.com/build/migrate-to-built-in-kotlin
- KSP migration: https://developer.android.com/build/migrate-to-ksp
- Compose BOM: https://developer.android.com/develop/ui/compose/bom/bom-mapping
- AndroidX releases: https://developer.android.com/jetpack/androidx/versions
- Lifecycle releases: https://developer.android.com/jetpack/androidx/releases/lifecycle
- Navigation releases: https://developer.android.com/jetpack/androidx/releases/navigation
- Paging releases: https://developer.android.com/jetpack/androidx/releases/paging
- Room releases: https://developer.android.com/jetpack/androidx/releases/room
- Retrofit releases: https://github.com/square/retrofit/releases
- Coil releases: https://coil-kt.github.io/coil/changelog/
- Koin releases/setup: https://insert-koin.io/docs/setup/koin/
- Kotlin serialization releases: https://github.com/Kotlin/kotlinx.serialization/releases
