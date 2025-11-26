--
-- Created by Dependently-Typed Lambda Calculus on 2024-08-29
-- theory
-- Author: hglabplh
--

{-# OPTIONS --without-K --safe #-}

module theory where

Theory : Set
Theory = {!!} -- need it?? -- look for a kind of enums and the log. signs
-- look at it and see howw logic can be placed in the record
record ActiveDataTraceRec (langelement emetaschema activedatameta: Set) : Set where
  constructor ActDataTrc
  field
    element: langelement -- s- error
    metaschema : emetaschema -- here is potential a s-error
    actdatameta : (eactivedata Set: Set outcdata )

    trace : ActiveDataTraceRec  -- here go on




