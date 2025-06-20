<?php
include('../include/session.php');
include('../include/system_connect.php');
?>
<!DOCTYPE html>
<html>
<head>
<link rel="icon" type="image/x-icon" href="../../assets/images/logo.png">
<link rel="stylesheet" href="https://www.w3schools.com/w3css/4/w3.css">
<style>
.w3-button {
    width:150px;
    border-radius:3px;
    margin:7px;
}
table {
  font-family: arial, sans-serif;
  border-collapse: collapse;
  width: 100%;
}

td, th {
  border: 1px solid #dddddd;
  text-align: left;
  padding: 8px;
}

tr:nth-child(even) {
  background-color: #dddddd;
}
</style>
</head>
<body>

<h2>Result</h2>
<a href="../dashboard"><button class="w3-button w3-green">Go Dashboard</button></a>
<table>
  <tr>
    <th>Sl No.</th>
    <th>Date</th>
    <th>Type</th>
    <th>Autistic Score</th>
    <th>Remark</th>
  </tr>
  <?php
    $qry=$mysqli->prepare("select score, out_of_score, level, process_id, level1_sts, `level2.1_sts`, `level2.2_sts`, level3_sts, level4_sts from result where user_id=?");
    $qry->bind_param('s',$user_id);
    $qry->execute();
    $qry->bind_result($score, $out_of_score, $level, $process_id, $level1_sts, $level2_1_sts, $level2_2_sts, $level3_sts, $level4_sts);
    while ($qry->fetch()) 
    { $k++;
      $a[$k]=$score;
      $b[$k]=$out_of_score;
      $c[$k]=$level;
      $d[$k]=$process_id;
      $e[$k]=$level1_sts;
      $f[$k]=$level2_1_sts;
      $g[$k]=$level2_2_sts;
      $h[$k]=$level3_sts;
      $j[$k]=$level4_sts;
    }
    for ($i=1; $i <=$k ; $i++)
    { $score=$a[$i];
      $out_of_score=$b[$i];
      $level=$c[$i];
      $process_id=$d[$i];
      $level1_sts=$e[$i];
      $level2_1_sts=$f[$i];
      $level2_2_sts=$g[$i];
      $level3_sts=$h[$i];
      $level4_sts=$j[$i];

      $qry=$mysqli->prepare("select * from result where process_id=?");
      $qry->bind_param('s',$process_id);
      $qry->execute();
      $qry->store_result();
      $row=$qry->num_rows;

      $count++;
    
      $date=explode("_",$process_id);
      $date=date("d-m-Y",strtotime($date[0]));
      
      if($level==1)
      { $wrong=$out_of_score-$score;
        $percentage=($wrong/$out_of_score)*100;
        if($percentage>70){$remark="Visit a specialist";}
        else{$remark="No Autism";}
        $percentage=$percentage." %";
        $type="Developmental Question Phase 1";
      }
      else if($level==2.1 || $level==2.2)
      { $remark="Combine Score will be calculate on Developmental Question Phase 2 & DSM V";
        $type="Developmental Question Phase 2";
        if ($level==2.2) 
        { $qry=$mysqli->prepare("select out_of_score, score from result where level=2.1 and process_id=?");
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

          $wrong=$combine_total-$combine_score;
          $per=($wrong/$combine_total)*100;

          $dsm_per=(($out_of_score-$score)/$out_of_score)*100;
          if($per<70){$remark="No Autism";}
          else{$remark="Proceed For Image Recognition Test";}
          $percentage="DSM V: ".$dsm_per."%<br> Combine Score: ".$per." %";
          $type="DSM V";
        }
        
      }
      elseif ($level==3) 
      { $percentage=$score."/".$out_of_score;
        $type="Object Identification";
        if ($score>=3) 
        { $remark="No Autism";
        }
        else 
        { $remark="Visit a specialist";
        }
      }
      elseif ($level==4) 
      { $percentage=($score/$out_of_score)*100;
        $type="Speech Recognition";
        if ($percentage>=70) 
        { $remark="No Autism";
        }
        else 
        { $remark="Visit a specialist";
        }
        $percentage=$percentage." % Words Match";
      }
      $sl++;
       ?>
        <tr>
            <td><?php echo $sl;?></td>
            <td><?php echo $date;?></td>
            <td><?php echo $type;?></td>
            <td><?php echo $percentage;?></td>
            <td><?php echo $remark;?></td>
        </tr>
        <?php
      if($row==$count)
      { $count=0;
        
            ?>
              <tr>
                <td colspan=4>Final Result: </td>
                <td><?php echo $remark;?></td>
              </tr>
            <?php
      }
    }
  ?>
  
 
</table>

</body>
</html>

