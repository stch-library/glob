(ns stch.glob-spec
  (:use speclj.core stch.glob))

(describe "glob"
  (it "match-glob"
    (should= "a" (match-glob "a" "a"))
    (should-be-nil (match-glob "a" "b"))
    (should= "ab" (match-glob "a*" "ab"))
    (should= "a" (match-glob "a*" "a"))
    (should= "abc" (match-glob "a*c" "abc"))
    (should= "abcd" (match-glob "a*d" "abcd"))
    (should= "ad" (match-glob "a*d" "ad"))
    (should= "abcdef" (match-glob "a*" "abcdef"))
    (should= "ab" (match-glob "a?" "ab"))
    (should-be-nil (match-glob "a?" "a"))
    (should= "ab" (match-glob "a{b,c}" "ab"))
    (should= "ac" (match-glob "a{b,c}" "ac"))
    (should= "ad" (match-glob "a{b,c,d}" "ad"))
    (should-be-nil (match-glob "a{b,c}" "ad"))
    (should= "abcdef" (match-glob (compile-pattern "a{b,c}*e?") "abcdef")))
  (it "match-globf"
    (should= "/a" (match-globf "/a" "/a"))
    (should= "/ab" (match-globf "/a*" "/ab"))
    (should= "/abcd" (match-globf "/a*" "/abcd"))
    (should= "/abcd" (match-globf "/a*/" "/abcd"))
    (should= "/a" (match-globf "/a*/" "/a/b"))
    (should= "/a/b" (match-globf "/a*/b" "/a/b"))
    (should= "/ab/c" (match-globf "/a*/c" "/ab/c"))))
