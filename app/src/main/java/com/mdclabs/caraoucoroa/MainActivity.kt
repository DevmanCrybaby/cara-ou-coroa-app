package com.mdclabs.caraoucoroa

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.mdclabs.caraoucoroa.R
import com.mdclabs.caraoucoroa.ui.theme.CaraOuCoroaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
        enableEdgeToEdge()
        setContent {
            CaraOuCoroaTheme {
                CaraOuCoroaApp()
            }
        }
    }
}

@Composable
fun CaraOuCoroaApp() {
    val context = LocalContext.current
    val activity = context as? Activity

    var semAnuncios24h by remember { mutableStateOf(isSemAnuncios24hAtivo(context)) }
    var progressoAnunciosAssistidos by remember { mutableIntStateOf(0) }

    val assistirAnuncioRecompensa = {
        if (mInterstitialAd != null && activity != null) {
            mInterstitialAd?.show(activity)
            carregarInterstitial(context)

            progressoAnunciosAssistidos++

            if (progressoAnunciosAssistidos >= 2) {
                ativar24hSemAnuncios(context)
                semAnuncios24h = true
                progressoAnunciosAssistidos = 0
                Toast.makeText(
                    context,
                    context.getString(R.string.anuncios_desativados_sucesso),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.anuncio_assistido_1),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.carregando_video),
                Toast.LENGTH_SHORT
            ).show()
            carregarInterstitial(context)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!semAnuncios24h) {
                AdBannerWithRewardFrame(
                    semAnunciosAtivo = semAnuncios24h,
                    progressoAnuncios = progressoAnunciosAssistidos,
                    onAssistirAnuncioClique = { assistirAnuncioRecompensa() }
                )
            }
        }
    ) { innerPadding ->
        CaraOuCoroa(
            modifier = Modifier.padding(innerPadding),
            semAnuncios24h = semAnuncios24h
        )
    }
}

@Composable
fun CaraOuCoroa(
    modifier: Modifier = Modifier,
    semAnuncios24h: Boolean = false
) {
    val textoInicial = stringResource(R.string.gire_moeda)
    var resultado by remember { mutableIntStateOf(R.drawable.moeda_cara) }
    var textoResultado by remember { mutableStateOf(textoInicial) }

    //Contadores
    var contadorCaras by remember { mutableIntStateOf(0) }
    var contadorCoroas by remember { mutableIntStateOf(0) }

    var quantidadeLancamentos by remember { mutableIntStateOf(2) }

    var modoMultiplo by remember { mutableStateOf(false) }
    var limitarConsecutivos by remember { mutableStateOf(false) }
    var maxConsecutivos by remember { mutableIntStateOf(3) }
    // Precisamos rastrear o que saiu por último e quantas vezes
    var ultimoResultado by remember { mutableIntStateOf(-1) } // -1 = nenhum, 0 = cara, 1 = coroa
    var repeticoesAtuais by remember { mutableIntStateOf(0) }

    // Estado para controlar o ângulo da animação
    var anguloRotacao by remember { mutableFloatStateOf(0f) }

    // Animação suave do ângulo
    val anguloAnimado by animateFloatAsState(
        targetValue = anguloRotacao,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "AnimacaoMoeda"
    )

    val context = LocalContext.current
    val activity = context as? Activity

    var contadorParaAnuncio by remember {mutableIntStateOf(0)}
    var proximoAnuncioEm by remember {mutableIntStateOf(5)}

    // Carrega o primeiro anuncio assim que a tela abre
    LaunchedEffect(Unit) {
        if (!semAnuncios24h) {
            carregarInterstitial(context)
        }
    }

    var somHabilitado by remember { mutableStateOf(true) }
    val focusManager = LocalFocusManager.current
    var textoQuantidade by remember {mutableStateOf("2")}

    val validarEConfirmar = {
        val apenasNumeros = textoQuantidade.filter { it.isDigit()}
        val numeros = apenasNumeros.toIntOrNull() ?: quantidadeLancamentos
        //Corrige para o interavalo entre 2 e 200
        val valorCorrigido = numeros.coerceIn(2, 200)

        quantidadeLancamentos = valorCorrigido
        textoQuantidade = valorCorrigido.toString()
    }

    val coroutineScope = rememberCoroutineScope()
    var estaGirando by remember { mutableStateOf(false) }

    val lancarMoeda = {
        focusManager.clearFocus() // Esconde o teclado se estiver aberto

        // Se a moeda não estiver girando no momento, executa
        if (!estaGirando) {
            estaGirando = true
            focusManager.clearFocus() // Esconde o teclado se estiver aberto

            if (somHabilitado) {
                tocarSom(context)
            }

            // Inicia a rotação visual
            anguloRotacao += 1800f
            textoResultado = context.getString(R.string.girando)

            coroutineScope.launch {
                val tempoInicio = System.currentTimeMillis()
                val duracaoGiro = 800L // Duração de 1.1 segundos

                // Enquanto estiver girando, fica alternando a imagem a cada 120ms
                while (System.currentTimeMillis() - tempoInicio < duracaoGiro) {
                    delay(120.milliseconds)
                    resultado = if (resultado == R.drawable.moeda_cara) {
                        R.drawable.moeda_coroa
                    } else {
                        R.drawable.moeda_cara
                    }
                }

        if (modoMultiplo) {
            validarEConfirmar()

            var caras = 0
            var coroas = 0
            repeat(quantidadeLancamentos) {
                if (Random.nextInt(2) == 0) caras++ else coroas++
            }
            contadorCaras += caras
            contadorCoroas += coroas
            resultado = if (caras >= coroas) R.drawable.moeda_cara else R.drawable.moeda_coroa
            textoResultado = context.getString(R.string.resultado_multiplo, caras, coroas)
        } else {
            var sorteio = Random.nextInt(2)

            if (limitarConsecutivos && sorteio == ultimoResultado && repeticoesAtuais >= maxConsecutivos - 1) {
                sorteio = if (sorteio == 0) 1 else 0
            }

            if (sorteio == ultimoResultado) {
                repeticoesAtuais++
            } else {
                ultimoResultado = sorteio
                repeticoesAtuais = 1
            }

            if (sorteio == 0) {
                resultado = R.drawable.moeda_cara
                textoResultado = context.getString(R.string.cara)
                contadorCaras++
            } else {
                resultado = R.drawable.moeda_coroa
                textoResultado = context.getString(R.string.coroa)
                contadorCoroas++
            }
        }

                // Incrementa o contador de jogadas se não estiver no modo 24h sem anúncios
                if (!semAnuncios24h) {
                    contadorParaAnuncio++

                    if (contadorParaAnuncio >= proximoAnuncioEm) {
                        contadorParaAnuncio = 0
                        proximoAnuncioEm = Random.nextInt(3, 9)

                        mInterstitialAd?.let { ad ->
                            activity?.let { act ->
                                ad.show(act)
                                carregarInterstitial(context)
                            }
                        }
                    }
                }

                estaGirando = false
            }
        }
    }


    // Box para sobrepor o ícone do alto-falante
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit){
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = textoResultado,
            fontSize = 28.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Image (
            painter = painterResource(id = resultado),
            contentDescription = "Moeda",
            modifier = Modifier
                .size(190.dp)
                .graphicsLayer {
                    rotationY = anguloAnimado
                    cameraDistance = 12f * density
                }.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ){
                    lancarMoeda()
                }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Seletor de quantidade
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                selected = !modoMultiplo,
                onClick = { modoMultiplo = false },
                label = { Text(stringResource(R.string.modo_unico)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = modoMultiplo,
                onClick = { modoMultiplo = true },
                label = { Text(stringResource(R.string.modo_multiplo)) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (modoMultiplo) {
            // Seletor de quantidade
            Text(text = stringResource(R.string.quantidade_lancamentos), fontSize = 16.sp)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                AutoRepeatButton(
                    onTick = {
                        if (quantidadeLancamentos > 2) {
                            quantidadeLancamentos--
                            textoQuantidade = quantidadeLancamentos.toString()
                        }
                    },
                    enabled = quantidadeLancamentos > 2
                ) { Text("-", fontSize = 24.sp) }

                // Campo sem bordas e sem marcador de seleção
                CompositionLocalProvider(
                    LocalTextSelectionColors provides TextSelectionColors(
                        handleColor = Color.Transparent,
                        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    BasicTextField(
                        value = textoQuantidade,
                        onValueChange = { novoTexto ->
                            val apenasNumeros = novoTexto.filter { it.isDigit() }

                            if (apenasNumeros.length <= 4) {
                                textoQuantidade = apenasNumeros
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done // Define a tecla "OK" no teclado
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                validarEConfirmar()
                                focusManager.clearFocus() // Esconde o teclado ao apertar OK
                            }
                        ),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .width(60.dp)
                            .onFocusChanged { focusState ->
                                // Se o usuário clicar fora mantendo o campo vazio, restaura o número 2
                                if (!focusState.isFocused) {
                                    validarEConfirmar()
                                }
                            }
                    )
                }
                AutoRepeatButton(
                    onTick = {
                        validarEConfirmar()
                        if (quantidadeLancamentos < 200) {
                            quantidadeLancamentos++
                            textoQuantidade = quantidadeLancamentos.toString()
                        }
                    },
                    enabled = quantidadeLancamentos < 200
                ) { Text("+", fontSize = 24.sp) }
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            // Opções de Limite Consecutivo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.evitar_repeticoes), fontSize = 16.sp)
                Switch(
                    checked = limitarConsecutivos,
                    onCheckedChange = { limitarConsecutivos = it },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .scale(0.8f)
                )
            }

            if (limitarConsecutivos) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.maximo_consecutivo, maxConsecutivos))
                    Slider(
                        value = maxConsecutivos.toFloat(),
                        onValueChange = { maxConsecutivos = it.toInt() },
                        valueRange = 2f..10f,
                        steps = 7,
                        modifier = Modifier
                            .width(150.dp)
                            .padding(start = 16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        //Quadro de pontuação
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = stringResource(R.string.placar_caras, contadorCaras), fontSize = 18.sp)
                Text(text = stringResource(R.string.placar_coroas, contadorCoroas), fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botão reset
            TextButton(onClick = {
                contadorCaras = 0
                contadorCoroas = 0
                textoResultado = textoInicial
                resultado = R.drawable.moeda_cara
            }) {
                Text(text = stringResource(R.string.zerar_placar), color = MaterialTheme.colorScheme.error)
            }
        }
    }
        // Ícone de ligar/desligar som no canto superior direito
        IconButton(
            onClick = { somHabilitado = !somHabilitado },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (somHabilitado) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                contentDescription = "Alternar Som"
            )
        }
    }
}

fun tocarSom(context: android.content.Context) {
    val mp = android.media.MediaPlayer.create(context, R.raw.coin_flip)
    mp.setOnCompletionListener { it.release() } // Libera a memória quando o som acabar
    mp.start()
}

@Composable
fun AutoRepeatButton(
    onTick: () -> Unit,
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Efeito que roda enquanto o botão estiver pressionado
    LaunchedEffect(isPressed) {
        if (isPressed){
            // Aguarda um tempo inicial (ex: 400ms) para ter certeza que é um "long press"
            // Isso evita que o loop dispare junto com o clique individual
            delay(400.milliseconds)

            var delayTime = 200L // 200ms entre incrementos
            while (true) {
                onTick()
                delay(delayTime.milliseconds)
                // Acelera o incremento após os primeiros cliques
                if (delayTime > 50L) delayTime -= 10L
            }
        }
    }

    Button (
        onClick = { onTick() }, // O clique individual é tratado pelo LaunchedEffect
        interactionSource = interactionSource,
        enabled = enabled
    ){
        content()
    }
}

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    // ID do bloco de anúncio de Banner de TESTE oficial do AdMob
    val testAdUnitId = "ca-app-pub-3940256099942544/6300978111"
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            // Banner Adaptativo (Anchored Adaptive Banner) para ocupar 100% da largura da tela
            val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
            AdView(context).apply {
                setAdSize(adSize)
                adUnitId = testAdUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

private const val PREFS_NAME = "cara_ou_coroa_prefs"
private const val KEY_ADS_FREE_UNTIL = "ads_free_until_timestamp"

fun isSemAnuncios24hAtivo(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val timestampExpiracao = prefs.getLong(KEY_ADS_FREE_UNTIL, 0L)
    return System.currentTimeMillis() < timestampExpiracao
}

fun ativar24hSemAnuncios(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val tempoExpiracao = System.currentTimeMillis() + (24 * 60 * 60 * 1000L) // 24 horas em ms
    prefs.edit().putLong(KEY_ADS_FREE_UNTIL, tempoExpiracao).apply()
}

@Composable
fun AdBannerWithRewardFrame(
    semAnunciosAtivo: Boolean,
    progressoAnuncios: Int,
    onAssistirAnuncioClique: () -> Unit
) {
    if (semAnunciosAtivo) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp) // Alinhado perfeitamente com a largura do box do placar
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Moldura/Botão no topo do anúncio (acompanha a largura total do placar)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAssistirAnuncioClique() },
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (progressoAnuncios == 1)
                        stringResource(R.string.assistir_anuncio_recompensa_2)
                    else
                        stringResource(R.string.assistir_anuncio_recompensa),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Anúncio de Banner adaptativo
        BannerAdView(modifier = Modifier.fillMaxWidth())
    }
}

// Variável global para guardar o anúncio carregado
var mInterstitialAd: InterstitialAd? = null

fun carregarInterstitial(context: android.content.Context) {
    val testInterstitialId = "ca-app-pub-3940256099942544/1033173712"
    val adRequest = AdRequest.Builder().build()

    InterstitialAd.load(
        context,
        testInterstitialId,
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CaraOuCoroaPreview() {
    CaraOuCoroaTheme {
        CaraOuCoroa()
    }
}