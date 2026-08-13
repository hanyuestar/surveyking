import React from "react";
import useBaseUrl from "@docusaurus/useBaseUrl";
import useThemeContext from "@theme/hooks/useThemeContext";

function HeroButtons() {
  const { isDarkTheme } = useThemeContext();

  return (
    <>
      <a
        className={
          "btn mb-4 sm:mb-0 font-bold py-4 bg-black text-white font-bold hover:bg-gray-900 hover:text-white uppercase border border-solid w-full sm:w-auto rounded"
        }
        href={useBaseUrl("docs/")}
        rel="noopener noreferrer"
        target="_blank"
      >
        快速开始
      </a>
    </>
  );
}

export default HeroButtons;
