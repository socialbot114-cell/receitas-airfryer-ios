# Receitas Airfryer

MVP Android nativo em Kotlin + Jetpack Compose, com experiência offline-first.

## Produto

- 30 receitas completas, incluindo 11 doces, com ingredientes, passos, tempo, temperatura, porções, calorias estimadas e orientações de pré-aquecimento/virada.
- Busca e filtros determinísticos, guia de 15 alimentos, favoritos e despensa persistidos com DataStore.
- Ranking por ingredientes disponíveis e modo cozinhar com timer baseado em timestamp, resistente a recomposição e background.
- Logo, adaptive icon, splash Android 12+ e artes gastronômicas procedurais próprias, sem dependência de rede.
- Consulte `ASSET_LICENSES.md` para origem e licença dos assets.

## Build local

```bash
./gradlew assembleDebug
./gradlew bundleRelease
./gradlew test
```

Saídas:

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/bundle/release/app-release.aab`

## Assinatura de publicação

O bundle local já está configurado com uma keystore de upload ignorada pelo Git. Para configurar em outra máquina, criar uma keystore própria e configurar as propriedades fora do controle de versão:

```properties
storeFile=/caminho/seguro/receitas-airfryer-upload.jks
storePassword=...
keyAlias=receitas-airfryer
keyPassword=...
```

Nunca versionar a keystore ou essas senhas. O pacote de publicação é `br.com.receitasairfryer.app`.

## Privacidade

O MVP não solicita acesso à rede, não exige conta e não coleta dados. Preferências e timers ficam apenas no dispositivo.
