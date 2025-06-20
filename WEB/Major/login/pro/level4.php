<?php
include('../include/session.php');
include('../include/system_connect.php');
$action=$_POST['action'];
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Speech Recognition</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
    <style>
        /* Basic Reset */
        body, h3, form, label, input {
            margin: 0;
            padding: 0;
            font-family: Arial, sans-serif;
        }

        body {
            background-color: #f4f4f4;
            padding: 20px;
        }

        h3 {
            text-align: center;
            color: #333;
            margin-bottom: 20px;
        }

        form {
            background-color: #fff;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            margin-bottom: 20px;
            width: 100%;
            max-width: 800px;
            margin: 0 auto;
        }

        label {
            display: block;
            margin-bottom: 8px;
            font-size: 16px;
            color: #333;
        }

        input[type="radio"] {
            margin-right: 10px;
        }

        .question-container {
            margin-bottom: 20px;
        }

        button {
            background-color: #4CAF50;
            color: white;
            padding: 12px 24px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            width: 100%;
            margin-top: 20px;
        }

        button:hover {
            background-color: #45a049;
        }

        /* Mobile Responsiveness */
        @media (max-width: 768px) {
            h3 {
                font-size: 22px;
            }

            form {
                padding: 15px;
                max-width: 100%;
            }

            label {
                font-size: 14px;
            }

            button {
                padding: 10px;
            }
        }
    </style>
</head>
<body>
    <h3>Speech Recognition</h3>
    <form action="level4" method="post">
        <!-- Question 1 -->
         <?php
            $qry=$mysqli->prepare("select string from speech_recognition order by id");
            $qry->execute();
            $qry->bind_result($string);
            while ($qry->fetch()) 
            { $i++;
              $a[$i]=$string;
            }
            for ($j=1; $j <=$i ; $j++) 
            { $string=$a[$j];
              ?>
                <div class="question-container">
                    <main class="container" role="main" aria-label="Voice recognition application">
                    <h2>Read the text below</h2>
                    <p class="try-say">Try saying: <span id="expectedPhrase"><?php echo $string;?></span></p>

                        <button id="start" aria-live="polite" aria-controls="result">🎤 Start Listening</button>
                        <!-- <button id="stop" disabled>⏹ Stop Listening</button> -->

                    <p id="result" aria-live="assertive" role="status">Waiting for input...</p>
  </main>
                </div>
              <?php
            }
         ?>   
        <!-- Submit Button -->
        <!-- <button type="submit" name="action" value="process">Submit</button> -->
    </form>
</body>
</html>
<script>
    const startBtn = document.getElementById('start');
    const stopBtn = document.getElementById('stop');
    const resultEl = document.getElementById('result');
    const expectedPhraseEl = document.getElementById('expectedPhrase');

    const targetLine = expectedPhraseEl.innerHTML;
    expectedPhraseEl.textContent = `"${targetLine}"`;

    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

    if (!SpeechRecognition) {
      alert("Sorry, your browser does not support the Web Speech API.");
      startBtn.disabled = true;
    } else {
      const recognition = new SpeechRecognition();
      recognition.lang = 'en-US';
      recognition.interimResults = false;
      recognition.maxAlternatives = 1;

      recognition.onresult = (event) => {
        const transcript = event.results[0][0].transcript;
        resultEl.textContent = `"${transcript}"`;
        resultEl.style.color = 'green';
    
        $.post("process_speech.php",
        {
            real_speech: targetLine.toLowerCase(),
            said_speech: transcript.toLowerCase()
        },
        function(data,status){
            if (data==1) {
                location.replace("../dashboard");
            }  
        });
      };

      recognition.onerror = (event) => {
        resultEl.textContent = 'Error occurred: ' + event.error;
        resultEl.style.color = '#f58e8e';
      };

      recognition.onend = () => {
        startBtn.disabled = false;
        stopBtn.disabled = true;
      };

      startBtn.onclick = () => {
        recognition.start();
        resultEl.textContent = "Listening...";
        resultEl.style.color = '#fff';
        startBtn.disabled = true;
        stopBtn.disabled = false;
      };

      stopBtn.onclick = () => {
        recognition.stop();
        resultEl.textContent = "Stopped listening.";
        startBtn.disabled = false;
        stopBtn.disabled = true;
      };
    }
  </script>