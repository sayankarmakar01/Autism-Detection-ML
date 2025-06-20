<?php
include('../include/session.php');
include('../include/system_connect.php');
$action=$_POST['action'];
if($action=="process")
{ $process_id=$_SESSION['process_id'];

  $qry=$mysqli->prepare("select dob from user where id=?");
  $qry->bind_param('s',$user_id);
  $qry->execute();
  $qry->bind_result($dob);
  while ($qry->fetch()) 
  { $dob=$dob;
  }
 
  $current_date = date('Y-m-d');
  $birth_date_obj = new DateTime($dob);
  $current_date_obj = new DateTime($current_date);
  $diff = $current_date_obj->diff($birth_date_obj);
  $age_years = $diff->y;
  $age_month = $diff->m;
  if($age_years>0){$age_month=($age_years*12)+$age_month;}
  if($age_month>=60){$level3_sts="y";}else{$level3_sts="n";}
  
  $l2_ids=$_POST['l2_ids'];
  $score=0;
  foreach($l2_ids as $l2_id)
  { $ans=$_POST['group'.$l2_id];
    
    $qry=$mysqli->prepare("select name from object_identify where id=?");
    $qry->bind_param('s',$l2_id);
    $qry->execute();
    $qry->bind_result($name);
    while ($qry->fetch()) 
    { $name=$name;
    }
    if($ans==$name){$score++;}

    $qry=$mysqli->prepare("insert into result_object (correct, answer, process_id) values(?, ?, ?)");
    $qry->bind_param('sss', $name, $ans, $process_id);
    $qry->execute();
  }

  $level=3;
  $out_of_score=$_POST['total'];
  $qry=$mysqli->prepare("insert into result (score, level, process_id, user_id, out_of_score, level3_sts) values(?, ?, ?, ?, ?, ?)");
  $qry->bind_param('ssssss',$score, $level, $process_id, $user_id, $out_of_score,$level3_sts);
  $qry->execute();

  if($level3_sts=="y")
  { header('location:level4');
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
    <title>Identify Object</title>
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
    <h3>Identify The Object</h3>
    <form action="level3" method="post">
        <!-- Question 1 -->
         <?php
            $qry=$mysqli->prepare("select id, name, url from object_identify order by id");
            $qry->execute();
            $qry->bind_result($q_id, $name,$url);
            while ($qry->fetch()) 
            { $i++;
              $a[$i]=$q_id;
              $b[$i]=$name;
              $c[$i]=$url;
            }
            for ($j=1; $j <=$i ; $j++) 
            { $q_id=$a[$j];
              $url=$c[$j];
              ?>
                <input type="hidden" name="l2_ids[]" value="<?php echo $q_id;?>">
                <div class="question-container">
                    <label><img src="<?php echo $url;?>" Width=200px;></label>
                    <?php
                         for ($k=1; $k <=$i ; $k++) 
                         { $name=$b[$k];
                           ?>
                                <input type="radio" required name="group<?php echo $q_id;?>" value="<?php echo $name;?>"> <?php echo $name;?>
                           <?php
                         }
                    ?>
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
