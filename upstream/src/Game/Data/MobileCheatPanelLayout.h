#pragma once

#include "../../Types/ElementTypes.h"

#include <algorithm>
#include <array>

namespace MobileCheatPanelLayout
{
enum ElementIndex
{
	Title,
	Result,
	CheatMode,
	Invincibility,
	RestoreResources,
	IncreaseMagicLevel,
	IncreasePlayerLevel,
	AddMoney,
	Back,
	ElementCount
};

using Layout = std::array<Rect, ElementCount>;

inline Rect getCenteredPanelRect(int screenWidth, int screenHeight)
{
	constexpr int SafeMargin = 24;
	constexpr int MaximumWidth = 900;
	constexpr int MaximumHeight = 760;
	const int availableWidth = std::max(1, screenWidth - SafeMargin * 2);
	const int availableHeight = std::max(1, screenHeight - SafeMargin * 2);
	const int width = std::min(MaximumWidth, availableWidth);
	const int height = std::min(MaximumHeight, availableHeight);
	return { (screenWidth - width) / 2, (screenHeight - height) / 2,
		width, height };
}

inline Layout calculate(const Rect& panel, bool mobile)
{
	Layout layout{};
	constexpr int ButtonCount = 7;
	if (!mobile)
	{
		constexpr int ButtonHeight = 32;
		constexpr int ButtonGap = 4;
		constexpr int TitleHeight = 24;
		constexpr int TitleGap = 4;
		constexpr int ResultHeight = 26;
		constexpr int ResultGap = 6;
		constexpr int HorizontalMargin = 12;
		constexpr int ButtonMaximumWidth = 380;
		const int contentHeight = TitleHeight + TitleGap + ResultHeight + ResultGap
			+ ButtonCount * ButtonHeight + (ButtonCount - 1) * ButtonGap;
		const int contentY = panel.y + std::max(4, (panel.h - contentHeight) / 2);
		const int buttonWidth = std::min(ButtonMaximumWidth,
			std::max(1, panel.w - HorizontalMargin * 2));
		const int buttonX = panel.x + (panel.w - buttonWidth) / 2;
		layout[Title] = { buttonX, contentY, buttonWidth, TitleHeight };
		layout[Result] = { buttonX, contentY + TitleHeight + TitleGap,
			buttonWidth, ResultHeight };
		int y = contentY + TitleHeight + TitleGap + ResultHeight + ResultGap;
		for (int index = CheatMode; index <= Back; ++index)
		{
			layout[index] = { buttonX, y, buttonWidth, ButtonHeight };
			y += ButtonHeight + ButtonGap;
		}
		return layout;
	}

	const int inset = std::min(8, std::max(0, std::min(panel.w, panel.h) / 8));
	const int innerX = panel.x + inset;
	const int innerY = panel.y + inset;
	const int innerW = std::max(1, panel.w - inset * 2);
	const int innerH = std::max(1, panel.h - inset * 2);
	const int columns = innerW >= 600 || innerH < 356 ? 2 : 1;
	const int rows = (ButtonCount + columns - 1) / columns;
	const int gap = innerH >= rows * 38 + 48 ? 4 : 2;
	const int titleGap = 2;
	const int resultGap = 2;
	const int titleHeight = std::min(22, std::max(1, innerH / 14));
	const int resultHeight = std::min(24, std::max(1, innerH / 12));
	const int fixedHeight = titleHeight + titleGap + resultHeight + resultGap
		+ (rows - 1) * gap;
	const int buttonHeight = std::max(1, (innerH - fixedHeight) / rows);
	const int titleY = innerY;
	layout[Title] = { innerX, titleY, innerW, titleHeight };
	const int resultY = titleY + titleHeight + titleGap;
	layout[Result] = { innerX, resultY, innerW, resultHeight };
	const int buttonY = resultY + resultHeight + resultGap;
	const int columnGap = columns == 2 ? 8 : 0;
	const int buttonWidth = std::max(1, (innerW - columnGap) / columns);
	for (int index = 0; index < ButtonCount; ++index)
	{
		const int row = index / columns;
		const int column = index % columns;
		const int width = column + 1 == columns
			? innerW - column * (buttonWidth + columnGap)
			: buttonWidth;
		layout[CheatMode + index] =
			{ innerX + column * (buttonWidth + columnGap),
			  buttonY + row * (buttonHeight + gap), width, buttonHeight };
	}
	return layout;
}
}
