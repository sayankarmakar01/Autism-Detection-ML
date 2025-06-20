<?php
    include('../include/session.php');
    include('../include/system_connect.php');

    $real_speech=$_POST['real_speech'];
    $said_speech=$_POST['said_speech'];

    $process_id=$_SESSION['process_id'];
    
    $real_words = explode(" ", $real_speech);
    $said_words = explode(" ", $said_speech);

    foreach ($real_words as $key => $value) 
    { $total_words++;
      if ($said_words[$key]==$value) 
      { $correct_words++;
      }
    }

    $qry=$mysqli->prepare("insert into result_speech (correct, answer, process_id, total_word, correct_word) values(?, ?, ?, ?, ?)");
    $qry->bind_param('sssss', $real_speech, $said_speech, $process_id, $total_words, $correct_words);
    $qry->execute();

    $level=4;
    $level4_sts="y";
    $qry=$mysqli->prepare("insert into result (score, level, process_id, user_id, out_of_score, level4_sts) values(?, ?, ?, ?, ?, ?)");
    $qry->bind_param('ssssss',$correct_words, $level, $process_id, $user_id, $total_words,$level4_sts);
    $qry->execute();

    $_SESSION['msg']="Test Complete";
    unset($_SESSION['process_id']);
    echo 1;
?>