<?php
include('../include/session.php');
include('../include/system_connect.php');
$action=$_POST['action'];
if($action=="process")
{ $process_id=$_SESSION['process_id'];

  $level2_sts="n";
  
  $l2_ids=$_POST['l2_ids'];
  $score=0;
  foreach($l2_ids as $l2_id)
  { $ans=$_POST['group'.$l2_id];
    
    $qry=$mysqli->prepare("select question, yes, no from dsm_v_question where id=?");
    $qry->bind_param('s',$l2_id);
    $qry->execute();
    $qry->bind_result($question,$yes,$no);
    while ($qry->fetch()) 
    { $question=$question;
      $yes=$yes;
      $no=$no;
    }
    if($yes==1){$correct="Yes";}elseif($no==1){$correct="No";}
    if($ans==$correct){$score++;}

    $qry=$mysqli->prepare("insert into result_dsm_v (question, answer, correct, process_id) values(?, ?, ?, ?)");
    $qry->bind_param('ssss',$question, $ans, $correct, $process_id);
    $qry->execute();
  }

  $level=2.2;
  $out_of_score=$_POST['total'];
  $qry=$mysqli->prepare("insert into result (score, level, process_id, user_id, out_of_score, `level2.2_sts`) values(?, ?, ?, ?, ?, ?)");
  $qry->bind_param('ssssss',$score, $level, $process_id, $user_id, $out_of_score,$level2_sts);
  $qry->execute();

  $qry=$mysqli->prepare("select out_of_score, score from result where level=2.1 and process_id=?");
  $qry->bind_param('s',$process_id);
  $qry->execute();
  $qry->bind_result($out_of_score, $score);
  while ($qry->fetch()) 
  { $out_of_score=$out_of_score;
    $score=$score;
  }

  $combine_total=$out_of_score;
  $combine_score=$score;

  $qry=$mysqli->prepare("select out_of_score, score from result where level=2.2 and process_id=?");
  $qry->bind_param('s',$process_id);
  $qry->execute();
  $qry->bind_result($out_of_score, $score);
  while ($qry->fetch()) 
  { $out_of_score=$out_of_score;
    $score=$score;
  }

  $combine_total+=$out_of_score;
  $combine_score+=$score;

  $per=($combine_score/$combine_total)*100;

  $per=100-$per;

  if($per>70)
  { $level2_sts="y";
    $qry=$mysqli->prepare("update result set `level2.2_sts`=? where level=? and process_id=?");
    $qry->bind_param('sss',$level2_sts, $level, $process_id);
    $qry->execute();
    header('location:level3');
  }
  else
  { $_SESSION['msg']="Test Complete";
    unset($_SESSION['process_id']);
    header('location:../dashboard');
  }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DSM V Questions</title>
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
    <h3>DSM V Question</h3>
    <form action="level2.2" method="post">
        <!-- Question 1 -->
         <?php
            $qry=$mysqli->prepare("select id, question from dsm_v_question order by id");
            $qry->execute();
            $qry->bind_result($q_id, $question);
            while ($qry->fetch()) 
            { $i++;
              ?>
                <input type="hidden" name="l2_ids[]" value="<?php echo $q_id;?>">
                <div class="question-container">
                    <label><?php echo $question;?></label>
                    <input type="radio" required name="group<?php echo $q_id;?>" value="Yes"> Yes
                    <input type="radio" name="group<?php echo $q_id;?>" value="No"> No
                </div>
              <?php
            }
         ?>
          <input type="hidden" name="total" value="<?php echo $i;?>">
        
        <!-- Submit Button -->
        <button type="submit" name="action" value="process">Submit</button>
    </form>
</body>
</html>
