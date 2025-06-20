<?php
$mysqli=new mysqli("localhost","root","","major");

$file = fopen("DSM.csv","r");
$i=0;
while (($emapData = fgetcsv($file)))
	                { $a[$i]=$emapData[0];
                      $b[$i]=$emapData[1];
                      $c[$i]=$emapData[2];
                      $i++;
                    }
                    $qry=$mysqli->prepare("insert into dsm_v_question (question, yes, no) values(?, ?, ?)");
                    for ($j=1; $j <$i; $j++) 
                    { $qu=$a[$j];
                      $y=$b[$j];
                      $n=$c[$j];

                    //   echo $qu."@".$y."@".$n."<br>";
                      $qry->bind_param('sss',$qu,$y,$n);
                      $qry->execute();
                    }
fclose($file);
?>
