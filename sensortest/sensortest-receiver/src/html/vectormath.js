var unitMatrix=[[1,0,0],
                [0,1,0],
                [0,0,1]];

function rotX(m,a) {
  var r= [[1,0,0],
          [0,Math.cos(a),-Math.sin(a)],
          [0,Math.sin(a),Math.cos(a)]];
  return multiply(r,m);
}

function rotY(m,a) {
  var r= [[Math.cos(a),0,Math.sin(a)],
          [0,1,0],
          [-Math.sin(a),0,Math.cos(a)]]
  return multiply(r,m);
}

function rotZ(m,a) {
  var r= [[Math.cos(a),-Math.sin(a),0],
          [Math.sin(a),Math.cos(a),0],
          [0,0,1]]
  return multiply(r,m);
}

function transform(m,v) {
  return [v[0]*m[0][0]+v[1]*m[0][1]+v[2]*m[0][2],
          v[0]*m[1][0]+v[1]*m[1][1]+v[2]*m[1][2],
          v[0]*m[2][0]+v[1]*m[2][1]+v[2]*m[2][2]];
}

function multiply(m,n) {
  return [ [ m[0][0]*n[0][0]+m[0][1]*n[1][0]+m[0][2]*n[2][0], m[0][0]*n[0][1]+m[0][1]*n[1][1]+m[0][2]*n[2][1], m[0][0]*n[0][2]+m[0][1]*n[1][2]+m[0][2]*n[2][2] ],
           [ m[1][0]*n[0][0]+m[1][1]*n[1][0]+m[1][2]*n[2][0], m[1][0]*n[0][1]+m[1][1]*n[1][1]+m[1][2]*n[2][1], m[1][0]*n[0][2]+m[1][1]*n[1][2]+m[1][2]*n[2][2] ],
           [ m[2][0]*n[0][0]+m[2][1]*n[1][0]+m[2][2]*n[2][0], m[2][0]*n[0][1]+m[2][1]*n[1][1]+m[2][2]*n[2][1], m[2][0]*n[0][2]+m[2][1]*n[1][2]+m[2][2]*n[2][2] ] ];
}


