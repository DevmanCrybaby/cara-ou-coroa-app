package com.mdclabs.caraoucoroa

import android.os.Bundle
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mdclabs.caraoucoroa.ui.theme.CaraOuCoroaTheme
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CaraOuCoroaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CaraOuCoroa(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CaraOuCoroa(modifier: Modifier = Modifier) {
    var resultado by remember { mutableIntStateOf(R.drawable.moeda_cara) }
    var textoResultado by remember { mutableStateOf("Jogue a moeda!") }

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

    val lancarMoeda = {
        focusManager.clearFocus() // Esconde o teclado se estiver aberto

        if (somHabilitado) {
            tocarSom(context)
        }

        anguloRotacao += 1800f

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
            textoResultado = "$caras Caras, $coroas Coroas"
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
                textoResultado = "Cara!"
                contadorCaras++
            } else {
                resultado = R.drawable.moeda_coroa
                textoResultado = "Coroa!"
                contadorCoroas++
            }
        }
    }

    // Box para sobrepor o ícone no canto superior direito
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
        modifier = modifier
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
                label = { Text("Único") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = modoMultiplo,
                onClick = { modoMultiplo = true },
                label = { Text("Múltiplo") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (modoMultiplo) {
            // Seletor de quantidade (O código que você já tem do Row com AutoRepeatButton)
            Text(text = "Quantidade de lançamentos:", fontSize = 16.sp)

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
                Text(text = "Evitar repetições longas", fontSize = 16.sp)
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
                    Text("Máximo consecutivo: $maxConsecutivos")
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

        /*Button(
            onClick = {
                lancarMoeda() },
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(text = "Lançar!", fontSize = 20.sp)
        }*/

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
                Text(text = "Caras: $contadorCaras", fontSize = 18.sp)
                Text(text = "Coroas: $contadorCoroas", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botão reset
            TextButton(onClick = {
                contadorCaras = 0
                contadorCoroas = 0
                textoResultado = "Jogue a moeda!"
                resultado = R.drawable.moeda_cara
            }) {
                Text(text = "Zerar Placar", color = MaterialTheme.colorScheme.error)
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
            kotlinx.coroutines.delay(400.milliseconds)

            var delayTime = 200L // 200ms entre incrementos
            while (true) {
                onTick()
                kotlinx.coroutines.delay(delayTime.milliseconds)
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

@Preview(showBackground = true)
@Composable
fun CaraOuCoroaPreview() {
    CaraOuCoroaTheme {
        CaraOuCoroa()
    }
}